package fiji.plugin.trackmate.detector.lacss;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotRoi;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class LacssDetector<T extends RealType<T> & NativeType<T>> implements SpotDetector<T> {
	private final static String BASE_ERROR_MESSAGE = "LacssDetector: ";

	protected final ImgPlus<T> img;

	protected final Interval interval;

	protected final Map< String, Object > settings;

	private final Logger logger;

	protected String baseErrorMessage;

	protected String errorMessage;

	protected long processingTime;

	protected List<Spot> spots;

    private final LacssClient client;

	public LacssDetector(
			final ImgPlus<T> img,
			final Interval interval,
			final Map< String, Object > settings,
			final LacssClient client ) 
	{
		this.img = img;
		this.interval = interval;
		this.settings = settings;
		Logger logger = ( Logger ) settings.get( Constants.KEY_LOGGER );
		this.logger = (logger == null) ? Logger.VOID_LOGGER : logger;
		this.baseErrorMessage = BASE_ERROR_MESSAGE;
		this.client = client;
	}

	private boolean getDetections(RandomAccessibleInterval<T> crop, LacssMsg.Settings settings)
	{
		long[] dims = crop.dimensionsAsLongArray();
		long n_ch = 1;
		long depth = 1;
		int ch_c = img.dimensionIndex(Axes.CHANNEL);
		int ch_y = img.dimensionIndex(Axes.Y);
		int ch_x = img.dimensionIndex(Axes.X);
		int ch_z = img.dimensionIndex(Axes.Z);

		if (ch_c != -1) {
			n_ch = dims[img.dimensionIndex(Axes.CHANNEL)];
		} 
		final long height = dims[ch_y];
		final long width = dims[ch_x];
		if (ch_z != -1) {
			depth = dims[ch_z];
		}
		final double[] calibration = TMUtils.getSpatialCalibration(img);

		ByteBuffer data = ByteBuffer.allocate((int) (depth * width * height * n_ch * Float.BYTES));

		RealType< T > in = Util.getTypeFromInterval( crop );
		Converter<RealType<T>, FloatType> converter = RealTypeConverters.getConverter( in, new FloatType());

		long [] pos = new long[dims.length];

		for (long idx = 0; idx < (int)(depth * width * height * n_ch) ; idx+=1) { // enfore z-y-x-c format
			if (ch_c != -1) {
				pos[ch_c] = idx % n_ch ;
			}
			pos[ch_x] = ( idx / n_ch ) % width + crop.min(ch_x);
			pos[ch_y] = (idx / (n_ch * width)) % height + crop.min(ch_y);
			if (ch_z != -1) {
				pos[ch_z] = idx / (n_ch * width * height);
			}

			FloatType value = new FloatType();
			converter.convert(crop.getAt(pos), value);

			data.putFloat(value.get());
		}

		LacssMsg.Image encoded_img = LacssMsg.Image.newBuilder()
				.setWidth(width)
				.setHeight(height)
				.setChannel(n_ch)
				.setDepth(depth)
				.setData(ByteString.copyFrom(data.array()))
				.build();

		LacssMsg.Input inputs = LacssMsg.Input.newBuilder()
				.setImage(encoded_img)
				.setSettings(settings)
				.build();

		LacssMsg.Results msg;
		try {

			// Logger.IJ_LOGGER.log("Connecting to server" + client.toString());

			msg = client.runDetection(inputs);
			
		} catch (InterruptedException e) {
			errorMessage = baseErrorMessage + e.getLocalizedMessage();
			return false;
			// logger.error(BASE_ERROR_MESSAGE + e.getLocalizedMessage());
		} 

		if (msg == null) {
			errorMessage = baseErrorMessage + client.status.getCode();
			Logger.IJ_LOGGER.error("server returned error code: " + client.status.getCode() + "\n");
			Logger.IJ_LOGGER.error(client.status.getDescription());
			return false;
		}

		int n_spots = msg.getRoisCount();
		spots = new ArrayList<>( n_spots );
		for ( LacssMsg.Roi roi : msg.getRoisList()) {
			if (depth == 1) {
				LacssMsg.Polygon polygon = roi.getPolygon();
				float score = polygon.getScore();
				List<LacssMsg.Point> points = polygon.getPointsList();
	
				double [] x = new double[points.size()];
				double [] y = new double[points.size()];

				int cnt = 0;
				for (LacssMsg.Point point : points) {
				
					x[cnt] = calibration[ch_x] * ( crop.min(ch_x) + point.getX() );
					y[cnt] = calibration[ch_y] * ( crop.min(ch_y) + point.getY() );

					cnt += 1;
				}

				spots.add(SpotRoi.createSpot(x, y, score * 100));

			} else {
				
				LacssMsg.Mesh mesh = roi.getMesh();
				float score = mesh.getScore();
				float zc = 0, yc = 0, xc = 0, zc2 = 0, yc2 = 0, xc2 = 0;

				for ( LacssMsg.Point vert : mesh.getVertsList()) {
					zc += vert.getZ();
					yc += vert.getY();
					xc += vert.getX();
					zc2 += vert.getZ() * vert.getZ()  ;
					yc2 += vert.getY() * vert.getY();
					xc2 += vert.getX() * vert.getX();
				}

				zc = zc / mesh.getVertsCount();
				yc = yc / mesh.getVertsCount();
				xc = xc / mesh.getVertsCount();
				zc2 = zc2 / mesh.getVertsCount() - zc * zc; 
				yc2 = yc2 / mesh.getVertsCount() - yc * yc;
				xc2 = xc2 / mesh.getVertsCount() - xc * xc;

				double radius = Math.sqrt((zc2 + yc2 + xc2)) / 3;

				spots.add(new Spot(
					(xc + crop.min(ch_x)) * calibration[0], 
					(yc + crop.min(ch_y)) * calibration[1],
					(zc + crop.min(ch_z)) * calibration[2],
					 radius, score*100));
			}
		}

		return true;
	}

	private float getFloat(String key)
	{
		Double v = (Double) settings.get(key);
		return v.floatValue();
	}

	@Override
	public boolean process() {
		final long start = System.currentTimeMillis();

		final RandomAccessibleInterval<T> rai = Views.interval(img, interval);

		LacssMsg.Settings settingMsg = LacssMsg.Settings.newBuilder()
			.setDetectionThreshold(getFloat(Constants.KEY_DETECTION_THRESHOLD))
			.setMinCellArea(getFloat(Constants.KEY_MIN_CELL_AREA))
			.setScaling(getFloat(Constants.KEY_SCALING))
			.setNmsIou(getFloat(Constants.KEY_NMS_IOU))
			.setSegmentationThreshold(getFloat(Constants.KEY_SEGMENTATION_THRESHOLD))
			.setDetectionThreshold(getFloat(Constants.KEY_DETECTION_THRESHOLD))
			.setReturnPolygon(true)
			.build();

		boolean status = getDetections(rai, settingMsg); // blocking

		final long end = System.currentTimeMillis();
		this.processingTime = end - start;

		return status;
	}

	@Override
	public List<Spot> getResult() {
		return spots;
	}

	@Override
	public boolean checkInput() {
		if (null == img) {
			errorMessage = baseErrorMessage + "Image is null.";
			return false;
		}
		// if (img.dimensionIndex(Axes.Z) >= 0) {
		// 	errorMessage = baseErrorMessage + "Image must be 2D over time, got an image with multiple Z.";
		// 	return false;
		// }
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}
}