package fiji.plugin.trackmate.detector.lacss;

import fiji.plugin.trackmate.detector.lacss.LacssDetectorConfigurationPanel.PretrainedModel;

public class Constants {
	/*
	 * CONSTANTS
	 */
	public static final String LACSS_DETECTOR_KEY = "LACSS_DETECTOR";

	// unused
	public static final String KEY_LACSS_MODEL = "LACSS_MODEL";
	public static final PretrainedModel DEFAULT_LACSS_MODEL = PretrainedModel.Default;

	public static final String KEY_LACSS_CUSTOM_MODEL_FILEPATH = "LACSS_MODEL_FILEPATH";
	public static final String DEFAULT_LACSS_CUSTOM_MODEL_FILEPATH = "";

	public static final String KEY_LACSS_REMOTE_SERVER = "LACSS_REMOTE_SERVER";
	public static final String DEFAULT_LACSS_REMOTE_SERVER = "localhost:7051";

	public static final String KEY_LACSS_REMOTE_SERVER_TOKEN = "LACSS_REMOTE_SERVER_TOKEN";
	public static final String DEFAULT_LACSS_REMOTE_SERVER_TOKEN = "";

	public static final String KEY_RETURN_LABEL = "RETURN_LABEL";
	public static final boolean DEFAULT_RETURN_LABEL = Boolean.valueOf(true);

	public static final String KEY_MIN_CELL_AREA = "MIN_CELL_AREA";
	public static final Double DEFAULT_MIN_CELL_AREA = Double.valueOf( 0. );

	public static final String KEY_REMOVE_OUT_OF_BOUNDS = "REMOVE_OUT_OF_BOUNDS"; 
	public static final Boolean DEFAULT_REMOVE_OUT_OF_BOUNDS = Boolean.valueOf(false);

	public static final String KEY_SCALING = "SCALING";
	public static final Double DEFAULT_SCALING = Double.valueOf( 1. );

	public static final String KEY_NMS_IOU = "NMS_IOU";
	public static final Double DEFAULT_NMS_IOU = Double.valueOf( 0. );

	public static final String KEY_SEGMENTATION_THRESHOLD = "SEGMENTATION_THRESHOLD";
	public static final Double DEFAULT_SEGMENTATION_THRESHOLD = Double.valueOf( 0.5 );
	
	public static final String KEY_DETECTION_THRESHOLD = "DETECTION_THRESHOLD";
	public static final Double DEFAULT_DETECTION_THRESHOLD = Double.valueOf( 0.45 );

	public static final String KEY_MULTI_CHANNEL = "MULTICHANNEL";
	public static final Boolean DEFAULT_MULTI_CHANNEL = Boolean.valueOf(true);


	public static final String KEY_LOGGER = "LOGGER";    
}
