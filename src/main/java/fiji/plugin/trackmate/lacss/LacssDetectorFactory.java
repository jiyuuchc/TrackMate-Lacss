package fiji.plugin.trackmate.lacss;

import static fiji.plugin.trackmate.io.IOUtils.readBooleanAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readStringAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeAttribute;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.lacss.LacssDetectorConfigurationPanel.PretrainedModel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotDetectorFactory.class )
public class LacssDetectorFactory< T extends RealType< T > & NativeType< T > > implements SpotDetectorFactory< T > 
{

	/*
	 * CONSTANTS
	 */
	public static final String LACSS_DETECTOR_KEY = "LACSS_DETECTOR";

	// unused
	public static final String KEY_LACSS_MODEL = "LACSS_MODEL";
	public static final String KEY_LACSS_PYTHON_FILEPATH = "LACSS_PYTHON_FILEPATH";
	public static final String DEFAULT_LACSS_PYTHON_FILEPATH = "/Fiji/plugins/TrackMate/lacss/lacss.py";
	public static final PretrainedModel DEFAULT_LACSS_MODEL = PretrainedModel.Default;
	// public static final String KEY_TARGET_CHANNEL = "LACSS_CHANNEL";
	// public static final int DEFAULT_TARGET_CHANNEL = 1;

	/**
	 * The key to the parameter that stores the path to the custom model file to
	 * use with Cellpose. It must be an absolute file path.
	 */
	public static final String KEY_LACSS_CUSTOM_MODEL_FILEPATH = "LACSS_MODEL_FILEPATH";
	public static final String DEFAULT_LACSS_CUSTOM_MODEL_FILEPATH = "";
	public static final String KEY_RETURN_LABEL = "RETURN_LABEL";
	public static final boolean DEFAULT_RETURN_LABEL = Boolean.valueOf(false);

	// detector parameter keys
	public static final String NAME = "Lacss detector";

	/**
	 * The key to the parameter that store the estimated cell diameter. Contrary
	 * to Cellpose, this must be specified in physical units (e.g. µm) and
	 * TrackMate wil do the conversion. Use 0 or a negative value to have
	 * Cellpose determine this automatically (but it will take a bit longer).
	 */
	public static final String KEY_MIN_CELL_AREA = "MIN_CELL_AREA";
	public static final Double DEFAULT_MIN_CELL_AREA = Double.valueOf( 0. );

	/**
	 * The key to the parameter that stores the logger instance, to which
	 * Cellpose messages wil be sent. Values must be implementing
	 * {@link Logger}. This parameter won't be serialized.
	 * 
	 */
	public static final String KEY_LOGGER = "LOGGER";

	// remove detections for which the predicted centroid is out of image bound
	public static final String KEY_REMOVE_OUT_OF_BOUNDS = "REMOVE_OUT_OF_BOUNDS"; 
	public static final Boolean DEFAULT_REMOVE_OUT_OF_BOUNDS = Boolean.valueOf(false);

	/** A image scaling factor. If not 1, the input image will be resized internally before fed to the model. The results 
	 * will be resized back to the scale of the orginal input image. **/
	public static final String KEY_SCALING = "SCALING";
	public static final Double DEFAULT_SCALING = Double.valueOf( 1. );

	/** iou threshold for the non-max-suppression post-processing. Default is 0, which disable non-max-suppression. */
	public static final String KEY_NMS_IOU = "NMS_IOU";
	public static final Double DEFAULT_NMS_IOU = Double.valueOf( 0. );

	/**  Segmentation threshold: Default = 0.5 ;*/
	public static final String KEY_SEGMENTATION_THRESHOLD = "SEGMENTATION_THRESHOLD";
	public static final Double DEFAULT_SEGMENTATION_THRESHOLD = Double.valueOf( 0.5 );
	
	/**  Detectiion threshold/Min Prediction scores: Default = 0.5 ;*/
	public static final String KEY_DETECTION_THRESHOLD = "SEGMENTATION_THRESHOLD";
	public static final Double DEFAULT_DETECTION_THRESHOLD = Double.valueOf( 0.5 );

	/** An html information text. */
	public static final String INFO_TEXT = "<html>"
			+ "This detector relies on deep-learning model Lacss to detect cells."
			+ "<p>"
			+ "The detector simply calls upon a pre-written python script."
			+ "To work, certain packages will be installed to run "
			+ "Please refer to the github of the lacss library: "
			+ "<u><a href=\"https://github.com/jiyuuchc/lacss\">https://github.com/jiyuuchc/lacss</a></u>"
			+ "<p>"
			+ "You will also need to specify the path to the <b>Python file</b> that can run lacss pipeline "
			+ "<p>"
			+ "Documentation for this module "
			+ "<a href=\"https://imagej.net/plugins/trackmate/trackmate-cellpose\">on the ImageJ Wiki</a>."
			+ "</html>";

	// resources
	// static final String PY_SCRIPT_PATH = "/scripts/lacss_server.py"; // resource path to the .py
	static final String MODEL_PATH = "/model/lacss_default.pkl"; // resource path to the model file

	/*
	 * FIELDS
	 */

	/** The image to operate on. Multiple frames, multiple channels. */
	protected ImgPlus< T > img;

	protected Map< String, Object > settings;

	protected String errorMessage;

	protected static Process pyServer = null; // the py process that does the computation

	// protected static String pyFilePath;

	protected static String modelPath;

	/*
	 * METHODS
	 */

	static String exportResource(String resourceName) throws IOException
	{
		InputStream stream = LacssDetectorFactory.class.getResourceAsStream(resourceName);
		if (stream == null) {
			throw new RuntimeException("Cannot find resource needed: " + resourceName);
		}
		File outfile = File.createTempFile("lacss_", "");
		FileUtils.copyInputStreamToFile(stream, outfile);

		return outfile.getAbsolutePath();
	}

	private static void addOnShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				// new File(pyFilePath).delete();
				new File(modelPath).delete();

				if (pyServer.isAlive()) {
					pyServer.destroy();
				}
			}
		}));
	}

	public static Process getPyServer()
	{
		if (pyServer == null) { // the server has not been started
			try {
				// pyFilePath = exportResource(PY_SCRIPT_PATH);
				modelPath = exportResource(MODEL_PATH);
				// String modelPath = new File(LacssDetectorFactory.class.getResource(MODEL_PATH).getFile()).getAbsolutePath();

				ProcessBuilder pb = new ProcessBuilder("python", "-m", "lacss.deploy.server", modelPath);
				pb.redirectError( ProcessBuilder.Redirect.INHERIT );

				pyServer = pb.start();

				//System.err.println(pyFilePath);

				addOnShutdownHook();
			}
			catch (IOException | NullPointerException e) {
				throw(new RuntimeException("Failed to start the python engine.\n" + e.getLocalizedMessage()));
			}
		} 
		else if (! pyServer.isAlive()) { // server died for some reason

			throw(new RuntimeException("The python engine died unexpectedly."));
		}

		return pyServer;
	}

	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		// final boolean simplifyContours = false; //( Boolean ) settings.get( KEY_SIMPLIFY_CONTOURS );
		// final double[] calibration = TMUtils.getSpatialCalibration( img );

		final ImgPlus< T > singleTimePoint;

		if ( img.dimensionIndex( Axes.TIME ) < 0 )
			singleTimePoint = img;
		else
			singleTimePoint = ImgPlusViews.hyperSlice( img, img.dimensionIndex( Axes.TIME ), frame );

		final LacssDetector< T > detector = new LacssDetector<T>(
				singleTimePoint,
				interval,
				settings,
				( Logger ) settings.get( KEY_LOGGER ),
				getPyServer()
		);

		return detector;
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
		boolean ok = true; // writeTargetChannel( settings, element, errorHolder );
		// ok = ok && writeAttribute( settings, element, KEY_LACSS_PYTHON_FILEPATH, String.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_LACSS_CUSTOM_MODEL_FILEPATH, String.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_MIN_CELL_AREA, Double.class, errorHolder );
		// ok = ok && writeAttribute( settings, element, KEY_RETURN_LABEL, Boolean.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_REMOVE_OUT_OF_BOUNDS, Boolean.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_SCALING, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_NMS_IOU, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_SEGMENTATION_THRESHOLD, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_DETECTION_THRESHOLD, Double.class, errorHolder );

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
		// ok = ok && readStringAttribute( element, settings, KEY_LACSS_PYTHON_FILEPATH, errorHolder );
		ok = ok && readStringAttribute( element, settings, KEY_LACSS_CUSTOM_MODEL_FILEPATH, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_MIN_CELL_AREA, errorHolder );
		// ok = ok && readBooleanAttribute( element, settings, KEY_RETURN_LABEL, errorHolder );
		ok = ok && readBooleanAttribute( element, settings, KEY_REMOVE_OUT_OF_BOUNDS, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_SCALING, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_NMS_IOU, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_SEGMENTATION_THRESHOLD, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_DETECTION_THRESHOLD, errorHolder );

		// Read model.
		final String str = element.getAttributeValue( KEY_LACSS_MODEL );
		if ( null == str )
		{
			errorHolder.append( "Attribute " + KEY_LACSS_MODEL + " could not be found in XML element.\n" );
			ok = false;
		}
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
		// settings.put( KEY_LACSS_PYTHON_FILEPATH, DEFAULT_LACSS_PYTHON_FILEPATH );
		// settings.put( KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL );
		// settings.put( KEY_OPTIONAL_CHANNEL_2, DEFAULT_OPTIONAL_CHANNEL_2 );
		settings.put( KEY_LACSS_MODEL, DEFAULT_LACSS_MODEL );
		settings.put( KEY_MIN_CELL_AREA, DEFAULT_MIN_CELL_AREA );
		// settings.put( KEY_RETURN_LABEL, DEFAULT_RETURN_LABEL );
		settings.put( KEY_REMOVE_OUT_OF_BOUNDS, false );
		settings.put( KEY_SCALING, DEFAULT_SCALING);
		settings.put( KEY_NMS_IOU, DEFAULT_NMS_IOU);
		settings.put ( KEY_SEGMENTATION_THRESHOLD, DEFAULT_SEGMENTATION_THRESHOLD);
		settings.put ( KEY_DETECTION_THRESHOLD, DEFAULT_DETECTION_THRESHOLD);
		settings.put( KEY_LOGGER, Logger.DEFAULT_LOGGER );
		settings.put( KEY_LACSS_CUSTOM_MODEL_FILEPATH, DEFAULT_LACSS_CUSTOM_MODEL_FILEPATH );
		return settings;
	}

	@Override
	public boolean checkSettings( final Map< String, Object > settings )
	{
		boolean ok = true;
		final StringBuilder errorHolder = new StringBuilder();
		// ok = ok & checkParameter( settings, KEY_LACSS_PYTHON_FILEPATH, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_LACSS_CUSTOM_MODEL_FILEPATH, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_LACSS_MODEL, PretrainedModel.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_MIN_CELL_AREA, Double.class, errorHolder );
		// ok = ok & checkParameter( settings, KEY_RETURN_LABEL, Boolean.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_REMOVE_OUT_OF_BOUNDS, Boolean.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_SCALING, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_NMS_IOU, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_SEGMENTATION_THRESHOLD, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_DETECTION_THRESHOLD, Double.class, errorHolder );		

		// If we have a logger, test it is of the right class.
		final Object loggerObj = settings.get( KEY_LOGGER );
		if ( loggerObj != null && !Logger.class.isInstance( loggerObj ) )
		{
			errorHolder.append( "Value for parameter " + KEY_LOGGER + " is not of the right class. "
					+ "Expected " + Logger.class.getName() + ", got " + loggerObj.getClass().getName() + ".\n" );
			ok = false;
		}

		final List< String > mandatoryKeys = Arrays.asList(
				// KEY_LACSS_PYTHON_FILEPATH,
				KEY_LACSS_MODEL,
				KEY_MIN_CELL_AREA,
				// KEY_RETURN_LABEL,
				KEY_REMOVE_OUT_OF_BOUNDS,
				KEY_SCALING,
				KEY_NMS_IOU,
				KEY_SEGMENTATION_THRESHOLD,
				KEY_DETECTION_THRESHOLD);
		final List< String > optionalKeys = Arrays.asList(
				KEY_LACSS_CUSTOM_MODEL_FILEPATH,
				KEY_LOGGER );
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
		return LACSS_DETECTOR_KEY;
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
