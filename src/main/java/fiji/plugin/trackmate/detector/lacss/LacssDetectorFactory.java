package fiji.plugin.trackmate.detector.lacss;

import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readStringAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeAttribute;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotDetectorFactory.class )
public class LacssDetectorFactory< T extends RealType< T > & NativeType< T > > implements SpotDetectorFactory< T > 
{
	// detector parameter keys
	public static final String NAME = "Lacss detector";

	/** An html information text. */
	public static final String INFO_TEXT = "<html>"
			+ "Use the deep-learning model 'Lacss' to detect cells."
			+ "<p>"
			+ "You should have the 'lacss-server' running either locally or on a remote network computer. The detector"
			+ "simply connects to the server via TCP and to obtain single cell segmentations of the image opened."
			+ "<p>"
			+ "Please refer to the github of the lacss library: "
			+ "<u><a href=\"https://github.com/jiyuuchc/lacss\">https://github.com/jiyuuchc/lacss</a></u>"
			+ "<p>"
			+ "Documentation for this module "
			+ "<a href=\"https://imagej.net/plugins/trackmate/trackmate-cellpose\">on the ImageJ Wiki</a>."
			+ "</html>";

	/*
	 * FIELDS
	 */

	/** The image to operate on. Multiple frames, multiple channels. */
	protected ImgPlus< T > img;

	protected long n_ch;

	protected Map< String, Object > settings;

	protected String errorMessage;

	// protected static String defaultModelPath = null;
	// protected static LacssClient localClient = null;
	protected static LacssClient remoteClient = null;

	// static initialization
	static {
		// exportResource("/model/lacss_default.pkl");
		addOnShutdownHook();
	}

	/*
	 * METHODS
	 */

	// static void exportResource(String resourceName)
	// {
	// 	InputStream stream = LacssDetectorFactory.class.getResourceAsStream(resourceName);

	// 	if (stream == null) {
	// 		throw new RuntimeException("Cannot find resource needed: " + resourceName);
	// 	}

	// 	try {

	// 		File outfile = File.createTempFile("lacss_", "");
	// 		FileUtils.copyInputStreamToFile(stream, outfile);

	// 		defaultModelPath = outfile.getAbsolutePath();

	// 	} catch (IOException e) {

	// 		throw new RuntimeException("Lacss: Cannot extract model parameters");

	// 	}

	// }

	private static void addOnShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
		{
			@Override
			public void run() {

				// new File(defaultModelPath).delete();

				// if (localClient != null) {

				// 	localClient.shutdownLocalProcess();
				// }
			}
		}));
	}

	// private String getModelPath() throws IOException
	// {
	// 	if (settings.get(Constants.KEY_LACSS_MODEL) == PretrainedModel.Default) {

	// 		return defaultModelPath;

	// 	} else {

	// 		return (String) settings.get(Constants.KEY_LACSS_CUSTOM_MODEL_FILEPATH);

	// 	}

	// }


	public LacssClient getClient() throws IOException 
	{
		// if (settings.get(Constants.KEY_LACSS_MODEL) == PretrainedModel.Remote) {

			String host = (String) settings.get(Constants.KEY_LACSS_REMOTE_SERVER);
			String token = (String) settings.get(Constants.KEY_LACSS_REMOTE_SERVER_TOKEN);

			remoteClient = new LacssClient(host, token);

			// Logger.IJ_LOGGER.log("Trying connecting to: " + host);
			// Logger.IJ_LOGGER.log("Using client object: " + remoteClient.toString());

			return remoteClient;

		// } else {

		// 	String modelPath = getModelPath().trim();

		// 	if ( localClient != null) {
		// 		Path oldPath = Paths.get(localClient.getModelPath()).normalize();
		// 		Path newPath = Paths.get(modelPath).normalize();

		// 		if (! oldPath.equals(newPath) ) {
		// 			// Logger.IJ_LOGGER.log("Stopping previous backend.");
		// 			// Logger.IJ_LOGGER.log("Old parameter file : " + oldPath);
	
		// 			localClient.shutdownLocalProcess();
		// 			localClient = null;	
		// 		}

		// 	}

		// 	if (localClient == null) {
		// 		// Logger.IJ_LOGGER.log("Startng new backend.");
		// 		// Logger.IJ_LOGGER.log("New parameter file : " + modelPath);

		// 		localClient = new LacssClient(modelPath);
		// 	}

		// 	return localClient;
		// }
	}

	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		final ImgPlus< T > singleTimePoint;

		// override interval to include all channels if possible
		FinalInterval itv;
		if ((Boolean) settings.get(Constants.KEY_MULTI_CHANNEL) && (n_ch == 2 || n_ch == 3)) {
			int ch_dim = img.dimensionIndex(Axes.CHANNEL);
			long [] mins = interval.minAsLongArray();
			long [] maxs = interval.maxAsLongArray();
			mins[ch_dim] = 0;
			maxs[ch_dim] = n_ch - 1;
			itv = new FinalInterval(mins, maxs);
		} else {
			itv = new FinalInterval(interval);
		}

		if ( img.dimensionIndex( Axes.TIME ) < 0 )
			singleTimePoint = img;
		else
			singleTimePoint = ImgPlusViews.hyperSlice( img, img.dimensionIndex( Axes.TIME ), frame );

		try {
			final LacssDetector< T > detector = new LacssDetector<T>(
					singleTimePoint,
					itv,
					settings,
					getClient()
			);

			return detector;

		} catch (IOException e) {

			String errMsg = "Unable to start the python backend. " + e.getLocalizedMessage();

			Logger.IJ_LOGGER.error(errMsg);

			return null;
		}
	}

	@Override
	public boolean forbidMultithreading()
	{
		return true;
	}

	@Override
	public boolean setTarget( final ImgPlus< T > img, final Map< String, Object > settings )
	{
		this.img = img;
		this.settings = settings;

		int dc = img.dimensionIndex(Axes.CHANNEL);
		if (dc < 0) {
			n_ch = 1;
		} else {
			long [] shape = img.dimensionsAsLongArray();
			n_ch = shape[img.dimensionIndex(Axes.CHANNEL)];
		}

		return checkSettings( settings );
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true; 
		ok = ok && writeAttribute( settings, element, Constants.KEY_MIN_CELL_AREA, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, Constants.KEY_SCALING, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, Constants.KEY_NMS_IOU, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, Constants.KEY_SEGMENTATION_THRESHOLD, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, Constants.KEY_DETECTION_THRESHOLD, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, Constants.KEY_MULTI_CHANNEL, Boolean.class, errorHolder );	
		ok = ok && writeAttribute( settings, element, Constants.KEY_LACSS_REMOTE_SERVER, String.class, errorHolder);
		ok = ok && writeAttribute( settings, element, Constants.KEY_LACSS_REMOTE_SERVER_TOKEN, String.class, errorHolder);

		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		settings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true;
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_MIN_CELL_AREA, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_SCALING, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_NMS_IOU, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_SEGMENTATION_THRESHOLD, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_DETECTION_THRESHOLD, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, Constants.KEY_MULTI_CHANNEL, errorHolder );
		ok = ok && readStringAttribute( element, settings, Constants.KEY_LACSS_REMOTE_SERVER, errorHolder);
		ok = ok && readStringAttribute( element, settings, Constants.KEY_LACSS_REMOTE_SERVER_TOKEN, errorHolder);

		return checkSettings( settings );
	}

	@Override
	public ConfigurationPanel getDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		return new LacssDetectorConfigurationPanel( settings, model );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > settings = new HashMap<>();
		settings.put( Constants.KEY_MIN_CELL_AREA, Constants.DEFAULT_MIN_CELL_AREA );
		settings.put( Constants.KEY_RETURN_LABEL, Constants.DEFAULT_RETURN_LABEL );
		settings.put( Constants.KEY_SCALING, Constants.DEFAULT_SCALING);
		settings.put( Constants.KEY_NMS_IOU, Constants.DEFAULT_NMS_IOU);
		settings.put( Constants.KEY_SEGMENTATION_THRESHOLD, Constants.DEFAULT_SEGMENTATION_THRESHOLD);
		settings.put( Constants.KEY_DETECTION_THRESHOLD, Constants.DEFAULT_DETECTION_THRESHOLD);
		settings.put( Constants.KEY_MULTI_CHANNEL, Constants.DEFAULT_MULTI_CHANNEL );
		settings.put( Constants.KEY_LACSS_REMOTE_SERVER, Constants.DEFAULT_LACSS_REMOTE_SERVER );
		settings.put( Constants.KEY_LACSS_REMOTE_SERVER_TOKEN, Constants.DEFAULT_LACSS_REMOTE_SERVER_TOKEN );

		settings.put( Constants.KEY_LOGGER, Logger.DEFAULT_LOGGER );

		return settings;
	}

	@Override
	public boolean checkSettings( final Map< String, Object > settings )
	{
		boolean ok = true;
		final StringBuilder errorHolder = new StringBuilder();
		ok = ok & checkParameter( settings, Constants.KEY_MIN_CELL_AREA, Double.class, errorHolder );
		ok = ok & checkParameter( settings, Constants.KEY_RETURN_LABEL, Boolean.class, errorHolder );
		ok = ok & checkParameter( settings, Constants.KEY_SCALING, Double.class, errorHolder );
		ok = ok & checkParameter( settings, Constants.KEY_NMS_IOU, Double.class, errorHolder );
		ok = ok & checkParameter( settings, Constants.KEY_SEGMENTATION_THRESHOLD, Double.class, errorHolder );
		ok = ok & checkParameter( settings, Constants.KEY_DETECTION_THRESHOLD, Double.class, errorHolder );		
		ok = ok & checkParameter( settings, Constants.KEY_MULTI_CHANNEL, Boolean.class, errorHolder );		
		ok = ok & checkParameter( settings, Constants.KEY_LACSS_REMOTE_SERVER, String.class, errorHolder );		
		ok = ok & checkParameter( settings, Constants.KEY_LACSS_REMOTE_SERVER_TOKEN, String.class, errorHolder );		

		// If we have a logger, test it is of the right class.
		final Object loggerObj = settings.get( Constants.KEY_LOGGER );
		if ( loggerObj != null && !Logger.class.isInstance( loggerObj ) )
		{
			errorHolder.append( "Value for parameter " + Constants.KEY_LOGGER + " is not of the right class. "
					+ "Expected " + Logger.class.getName() + ", got " + loggerObj.getClass().getName() + ".\n" );
			ok = false;
		}

		final List< String > mandatoryKeys = Arrays.asList(
			Constants.KEY_LACSS_REMOTE_SERVER,
			Constants.KEY_LACSS_REMOTE_SERVER_TOKEN);

		final List< String > optionalKeys = Arrays.asList(
			Constants.KEY_MIN_CELL_AREA,
			Constants.KEY_SCALING,
			Constants.KEY_NMS_IOU,
			Constants.KEY_MULTI_CHANNEL,
			Constants.KEY_SEGMENTATION_THRESHOLD,
			Constants.KEY_DETECTION_THRESHOLD,
			Constants.KEY_RETURN_LABEL,
			Constants.KEY_LOGGER );

		ok = ok & checkMapKeys( settings, mandatoryKeys, optionalKeys, errorHolder );
		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return Constants.LACSS_DETECTOR_KEY;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean has2Dsegmentation()
	{
		return true;
	}

	@Override
	public SpotDetectorFactoryBase< T > copy()
	{
		return new LacssDetectorFactory<>();
	}    
}
