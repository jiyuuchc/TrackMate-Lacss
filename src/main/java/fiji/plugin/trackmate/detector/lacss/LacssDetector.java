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
import io.grpc.StatusRuntimeException;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
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
		int ch_c = img.dimensionIndex(Axes.CHANNEL);
		int ch_y = img.dimensionIndex(Axes.Y);
		int ch_x = img.dimensionIndex(Axes.X);
		if (ch_c != -1) {
			n_ch = dims[img.dimensionIndex(Axes.CHANNEL)];
		} 
		final long height = dims[ch_y];
		final long width = dims[ch_x];
		final double[] calibration = TMUtils.getSpatialCalibration(img);

		ByteBuffer data = ByteBuffer.allocate((int) (width * height * n_ch * Float.BYTES));
		RandomAccessibleInterval<FloatType> floatImg = RealTypeConverters.convert(crop, new FloatType());
		long [] pos = new long[dims.length];
		for (long idx = 0; idx < (int)(width * height * n_ch) ; idx+=1) { // enfore y-x-c format
			if (ch_c != -1) {
				pos[ch_c] = idx % n_ch ;
			}
			pos[ch_x] = ( idx / n_ch ) % width;
			pos[ch_y] = idx / (n_ch * width);
			data.putFloat(floatImg.getAt(pos).get());
		}

		LacssMsg.Image encoded_img = LacssMsg.Image.newBuilder()
				.setWidth(width)
				.setHeight(height)
				.setChannel(n_ch)
				.setData(ByteString.copyFrom(data.array()))
				.build();

		LacssMsg.Input inputs = LacssMsg.Input.newBuilder()
				.setImage(encoded_img)
				.setSettings(settings)
				.build();

		LacssMsg.PolygonResult msg;
		try {

			// Logger.IJ_LOGGER.log("Connecting to server" + client.toString());

			msg = client.runDetection(inputs);

		} catch (StatusRuntimeException e) {
			// logger.error(BASE_ERROR_MESSAGE + "Unable to communicate with the server.");
			logger.error(BASE_ERROR_MESSAGE + e.getLocalizedMessage());
			return false;
		} 

		spots = new ArrayList<>( msg.getPolygonsCount() );
		for ( LacssMsg.Polygon polygon : msg.getPolygonsList()) {

			float score = polygon.getScore();
			List<LacssMsg.Point> points = polygon.getPointsList();
			double [] x = new double[points.size()];
			double [] y = new double[points.size()];

			int cnt = 0;
			for (LacssMsg.Point point : points) {
			
				x[cnt] = calibration[0] * ( interval.min(0) + point.getX() + 1.5 );
				y[cnt] = calibration[1] * ( interval.min(1) + point.getY() + 1.5 );

				cnt += 1;
			}

			spots.add(SpotRoi.createSpot(x, y, score * 100));
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
			.setRemoveOutOfBound((boolean)settings.get(Constants.KEY_REMOVE_OUT_OF_BOUNDS))
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
		if (img.dimensionIndex(Axes.Z) >= 0) {
			errorMessage = baseErrorMessage + "Image must be 2D over time, got an image with multiple Z.";
			return false;
		}
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