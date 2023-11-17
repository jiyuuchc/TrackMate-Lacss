package fiji.plugin.trackmate.lacss;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LACSS_CUSTOM_MODEL_FILEPATH;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LACSS_MODEL;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LOGGER;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_MIN_CELL_AREA;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_NMS_IOU;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_REMOVE_OUT_OF_BOUNDS;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_SCALING;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_SEGMENTATION_THRESHOLD;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.DetectionPreview;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class LacssDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final String TITLE = LacssDetectorFactory.NAME;

	private static final ImageIcon ICON = LacssUtils.logo64();

	private static final NumberFormat MIN_CELL_AREA_FORMAT = new DecimalFormat( "#.#" );

	private static final NumberFormat MIN_SCALING_FORMAT = new DecimalFormat( "#.#" );

	private static final NumberFormat MIN_NMS_IOU_FORMAT = new DecimalFormat( "#.#" );

	private static final NumberFormat MIN_SEGMENTATION_THRESHOLD_FORMAT = new DecimalFormat( "#.#" );

	protected static final String DOC1_URL = "https://jiyuuchc.github.io/lacss/api/deploy/#lacss.deploy.Predictor";

	// private final JButton btnBrowseLacssPath;

	// private final JTextField tfLacssExecutable;

	private final JComboBox< PretrainedModel > cmbboxPretrainedModel;

	private final JFormattedTextField ftfmin_cell_area;

	private final JFormattedTextField ftfmin_scaling;

	private final JFormattedTextField ftfnms_iou;

	private final JFormattedTextField ftfsegmentation_threshold;

	private final JCheckBox chckbxBounds;

	private final Logger logger;

	// private final JCheckBox chckbx_return_label;

	private final JTextField tfCustomPath;

	private final JButton btnBrowseCustomModel;

	public LacssDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		this.logger = model.getLogger();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., .1 };
		gridBagLayout.columnWidths = new int[] { 144, 0, 32 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblDetector = new JLabel( TITLE, ICON, JLabel.RIGHT );
		lblDetector.setFont( BIG_FONT );
		lblDetector.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblDetector = new GridBagConstraints();
		gbcLblDetector.gridwidth = 3;
		gbcLblDetector.insets = new Insets( 0, 5, 5, 0 );
		gbcLblDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDetector.gridx = 0;
		gbcLblDetector.gridy = 0;
		add( lblDetector, gbcLblDetector );

		final String text = "Click here for the documentation";
		final JLabel lblUrl = new JLabel( text );
		lblUrl.setHorizontalAlignment( SwingConstants.CENTER );
		lblUrl.setForeground( Color.BLUE.darker() );
		lblUrl.setFont( FONT.deriveFont( Font.ITALIC ) );
		lblUrl.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( DOC1_URL ) );
				}
				catch ( URISyntaxException | IOException ex )
				{
					ex.printStackTrace();
				}
			}

			@Override
			public void mouseExited( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( text );
			}

			@Override
			public void mouseEntered( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( "<html><a href=''>" + DOC1_URL + "</a></html>" );
			}
		} );

		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.gridwidth = 3;
		gbcLblUrl.insets = new Insets( 0, 10, 5, 15 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 1;
		add( lblUrl, gbcLblUrl );

		/*
		 * Path to Python or Lacss.
		 */

		// final JLabel lblCusstomModelFile = new JLabel( "Path to Lacss / python executable:" );
		// lblCusstomModelFile.setFont( FONT );
		// final GridBagConstraints gbcLblCusstomModelFile = new GridBagConstraints();
		// gbcLblCusstomModelFile.gridwidth = 2;
		// gbcLblCusstomModelFile.anchor = GridBagConstraints.SOUTHWEST;
		// gbcLblCusstomModelFile.insets = new Insets( 0, 5, 5, 5 );
		// gbcLblCusstomModelFile.gridx = 0;
		// gbcLblCusstomModelFile.gridy = 2;
		// add( lblCusstomModelFile, gbcLblCusstomModelFile );

		// btnBrowseLacssPath = new JButton( "Browse" );
		// btnBrowseLacssPath.setFont( FONT );
		// final GridBagConstraints gbcBtnBrowseLacssPath = new GridBagConstraints();
		// gbcBtnBrowseLacssPath.insets = new Insets( 0, 0, 5, 0 );
		// gbcBtnBrowseLacssPath.anchor = GridBagConstraints.SOUTHEAST;
		// gbcBtnBrowseLacssPath.gridx = 2;
		// gbcBtnBrowseLacssPath.gridy = 2;
		// add( btnBrowseLacssPath, gbcBtnBrowseLacssPath );

		// tfLacssExecutable = new JTextField( "" );
		// tfLacssExecutable.setFont( SMALL_FONT );
		// final GridBagConstraints gbcTfLacss = new GridBagConstraints();
		// gbcTfLacss.gridwidth = 3;
		// gbcTfLacss.insets = new Insets( 0, 5, 5, 0 );
		// gbcTfLacss.fill = GridBagConstraints.BOTH;
		// gbcTfLacss.gridx = 0;
		// gbcTfLacss.gridy = 3;
		// add( tfLacssExecutable, gbcTfLacss );
		// tfLacssExecutable.setColumns( 15 );

		/*
		 * Pretrained model.
		 */

		final JLabel lblPretrainedModel = new JLabel( "Pretrained model:" );
		lblPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblPretrainedModel = new GridBagConstraints();
		gbcLblPretrainedModel.anchor = GridBagConstraints.EAST;
		gbcLblPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblPretrainedModel.gridx = 0;
		gbcLblPretrainedModel.gridy = 6;
		add( lblPretrainedModel, gbcLblPretrainedModel );

		cmbboxPretrainedModel = new JComboBox<>( new Vector<>( Arrays.asList( PretrainedModel.values() ) ) );
		cmbboxPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcCmbboxPretrainedModel = new GridBagConstraints();
		gbcCmbboxPretrainedModel.gridwidth = 2;
		gbcCmbboxPretrainedModel.insets = new Insets( 0, 5, 5, 0 );
		gbcCmbboxPretrainedModel.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxPretrainedModel.gridx = 1;
		gbcCmbboxPretrainedModel.gridy = 6;
		add( cmbboxPretrainedModel, gbcCmbboxPretrainedModel );

		/*
		 * Custom model.
		 */

		final JLabel lblPathToCustomModel = new JLabel( "Path to custom model:" );
		lblPathToCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbcLblPathToCustomModel = new GridBagConstraints();
		gbcLblPathToCustomModel.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblPathToCustomModel.gridwidth = 2;
		gbcLblPathToCustomModel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblPathToCustomModel.gridx = 0;
		gbcLblPathToCustomModel.gridy = 4;
		add( lblPathToCustomModel, gbcLblPathToCustomModel );

		btnBrowseCustomModel = new JButton( "Browse" );
		btnBrowseCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbcBtnBrowseCustomModel = new GridBagConstraints();
		gbcBtnBrowseCustomModel.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseCustomModel.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowseCustomModel.gridx = 2;
		gbcBtnBrowseCustomModel.gridy = 4;
		add( btnBrowseCustomModel, gbcBtnBrowseCustomModel );

		tfCustomPath = new JTextField( " " );
		tfCustomPath.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		tfCustomPath.setColumns( 15 );
		final GridBagConstraints gbcTfCustomPath = new GridBagConstraints();
		gbcTfCustomPath.gridwidth = 3;
		gbcTfCustomPath.insets = new Insets( 0, 5, 5, 0 );
		gbcTfCustomPath.fill = GridBagConstraints.BOTH;
		gbcTfCustomPath.gridx = 0;
		gbcTfCustomPath.gridy = 5;
		add( tfCustomPath, gbcTfCustomPath );

		/*
		* Min Cell Area.
		*/

		final JLabel lblMin_cell_area = new JLabel( "Minimum Cell Area:" );
		lblMin_cell_area.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblMin_cell_area = new GridBagConstraints();
		gbcLblMin_cell_area.anchor = GridBagConstraints.EAST;
		gbcLblMin_cell_area.insets = new Insets( 0, 5, 5, 5 );
		gbcLblMin_cell_area.gridx = 0;
		gbcLblMin_cell_area.gridy = 9;
		add( lblMin_cell_area, gbcLblMin_cell_area );

		ftfmin_cell_area = new JFormattedTextField( MIN_CELL_AREA_FORMAT );
		ftfmin_cell_area.setHorizontalAlignment( SwingConstants.CENTER );
		ftfmin_cell_area.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfmin_cell_area = new GridBagConstraints();
		gbcFtfmin_cell_area.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfmin_cell_area.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfmin_cell_area.gridx = 1;
		gbcFtfmin_cell_area.gridy = 9;
		add( ftfmin_cell_area, gbcFtfmin_cell_area );

		final JLabel lblSpaceUnits = new JLabel( model.getSpaceUnits() );
		lblSpaceUnits.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSpaceUnits = new GridBagConstraints();
		gbcLblSpaceUnits.insets = new Insets( 0, 5, 5, 0 );
		gbcLblSpaceUnits.gridx = 2;
		gbcLblSpaceUnits.gridy = 9;
		add( lblSpaceUnits, gbcLblSpaceUnits );

		//* Return Label */

		// chckbx_return_label = new JCheckBox( "Return Label:" );
		// chckbx_return_label.setHorizontalTextPosition( SwingConstants.LEFT );
		// chckbx_return_label.setFont( SMALL_FONT );
		// final GridBagConstraints gbcChckbx_return_label = new GridBagConstraints();
		// gbcChckbx_return_label.anchor = GridBagConstraints.EAST;
		// gbcChckbx_return_label.insets = new Insets( 0, 0, 0, 5 );
		// gbcChckbx_return_label.gridx = 0;
		// gbcChckbx_return_label.gridy = 13;
		// add( chckbx_return_label, gbcChckbx_return_label );

		/* Scaling Factor*/

		final JLabel lblMin_scaling = new JLabel( "Scaling Factor:" );
		lblMin_scaling.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblMin_scaling = new GridBagConstraints();
		gbcLblMin_scaling.anchor = GridBagConstraints.EAST;
		gbcLblMin_scaling.insets = new Insets( 0, 5, 5, 5 );
		gbcLblMin_scaling.gridx = 0;
		gbcLblMin_scaling.gridy = 10;
		add( lblMin_scaling, gbcLblMin_scaling );

		ftfmin_scaling = new JFormattedTextField( MIN_SCALING_FORMAT );
		ftfmin_scaling.setHorizontalAlignment( SwingConstants.CENTER );
		ftfmin_scaling.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfmin_scaling = new GridBagConstraints();
		gbcFtfmin_scaling.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfmin_scaling.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfmin_scaling.gridx = 1;
		gbcFtfmin_scaling.gridy = 10;
		add( ftfmin_scaling, gbcFtfmin_scaling );

		/* nms_iou: Optional iou threshold for the non-max-suppression post-processing. Default is 0, which disable non-max-suppression.  */

		final JLabel lblnms_iou = new JLabel( "IOU Treshold:" );
		lblnms_iou.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblNms_iou = new GridBagConstraints();
		gbcLblNms_iou.anchor = GridBagConstraints.EAST;
		gbcLblNms_iou.insets = new Insets( 0, 5, 5, 5 );
		gbcLblNms_iou.gridx = 0;
		gbcLblNms_iou.gridy = 11;
		add( lblnms_iou, gbcLblNms_iou );

		ftfnms_iou = new JFormattedTextField( MIN_NMS_IOU_FORMAT );
		ftfnms_iou.setHorizontalAlignment( SwingConstants.CENTER );
		ftfnms_iou.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfnms_iou = new GridBagConstraints();
		gbcFtfnms_iou.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfnms_iou.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfnms_iou.gridx = 1;
		gbcFtfnms_iou.gridy = 11;
		add( ftfnms_iou, gbcFtfnms_iou );

		/* 	The minimal prediction scores.: Default : 0.5 */

		final JLabel lblsegmentation_threshold = new JLabel( "Segmentation Treshold:" );
		lblsegmentation_threshold.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentation_threshold = new GridBagConstraints();
		gbcLblSegmentation_threshold.anchor = GridBagConstraints.EAST;
		gbcLblSegmentation_threshold.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentation_threshold.gridx = 0;
		gbcLblSegmentation_threshold.gridy = 12;
		add( lblsegmentation_threshold, gbcLblSegmentation_threshold );

		ftfsegmentation_threshold = new JFormattedTextField( MIN_SEGMENTATION_THRESHOLD_FORMAT );
		ftfsegmentation_threshold.setHorizontalAlignment( SwingConstants.CENTER );
		ftfsegmentation_threshold.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfsegmentation_threshold = new GridBagConstraints();
		gbcFtfsegmentation_threshold.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfsegmentation_threshold.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfsegmentation_threshold.gridx = 1;
		gbcFtfsegmentation_threshold.gridy = 12;
		add( ftfsegmentation_threshold, gbcFtfsegmentation_threshold );

		/*
		 * Preview.
		 */

		final GridBagConstraints gbcBtnPreview = new GridBagConstraints();
		gbcBtnPreview.gridwidth = 3;
		gbcBtnPreview.fill = GridBagConstraints.BOTH;
		gbcBtnPreview.insets = new Insets( 0, 5, 5, 5 );
		gbcBtnPreview.gridx = 0;
		gbcBtnPreview.gridy = 16;

		final DetectionPreview detectionPreview = DetectionPreview.create()
				.model( model )
				.settings( settings )
				.detectorFactory( new LacssDetectorFactory<>() )
				.detectionSettingsSupplier( () -> getSettings() )
				.axisLabel( "Area histogram" )
				.get();
		add( detectionPreview.getPanel(), gbcBtnPreview );

		/*
		 * Check if GPU Detected.
		 */

		// final GridBagConstraints gbcBtncheck_gpu = new GridBagConstraints();
		// gbcBtncheck_gpu.gridwidth = 3;
		// gbcBtncheck_gpu.fill = GridBagConstraints.BOTH;
		// gbcBtncheck_gpu.insets = new Insets( 0, 5, 5, 5 );
		// gbcBtncheck_gpu.gridx = 2;
		// gbcBtncheck_gpu.gridy = 16;

		// final DetectionPreview detectioncheck_gpu = DetectionPreview.create()
		// 		.model( model )
		// 		.settings( settings )
		// 		.detectorFactory( new LacssDetectorFactory<>() )
		// 		.detectionSettingsSupplier( () -> getSettings() )
		// 		.axisLabel( "Area histogram" )
		// 		.get();
		// add( detectioncheck_gpu.getPanel(), gbcBtncheck_gpu );

		//* Out of Bounds Check Box Button */

		chckbxBounds = new JCheckBox( "Remove Out of Bounds:" );
		chckbxBounds.setHorizontalTextPosition( SwingConstants.LEFT );
		chckbxBounds.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxBounds = new GridBagConstraints();
		gbcChckbxBounds.anchor = GridBagConstraints.EAST;
		gbcChckbxBounds.gridwidth = 2;
		gbcChckbxBounds.insets = new Insets( 0, 5, 0, 0 );
		gbcChckbxBounds.gridx = 1;
		gbcChckbxBounds.gridy = 13;
		add( chckbxBounds, gbcChckbxBounds );

		/*
		 * Listeners and specificities.
		 */

		final ItemListener l3 = e -> {
			final boolean isCustom = cmbboxPretrainedModel.getSelectedItem() == PretrainedModel.CUSTOM;
			tfCustomPath.setVisible( isCustom );
			lblPathToCustomModel.setVisible( isCustom );
			btnBrowseCustomModel.setVisible( isCustom );
		};
		cmbboxPretrainedModel.addItemListener( l3 );
		l3.itemStateChanged( null );

		// btnBrowseLacssPath.addActionListener( l -> browseLacssPath() );
		
		btnBrowseCustomModel.addActionListener( l -> browseCustomModelPath() );
	}

	protected void browseCustomModelPath()
	{
		btnBrowseCustomModel.setEnabled( false );
		try
		{
			final File file = FileChooser.chooseFile( this, tfCustomPath.getText(), null,
					"Browse to a Lacss custom model", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file != null )
				tfCustomPath.setText( file.getAbsolutePath() );
		}
		finally
		{
			btnBrowseCustomModel.setEnabled( true );
		}
	}

	// protected void browseLacssPath()
	// {
	// 	btnBrowseLacssPath.setEnabled( false );
	// 	try
	// 	{
	// 		final File file = FileChooser.chooseFile( this, tfLacssExecutable.getText(), null,
	// 				"Browse to the Lacss Python executable", DialogType.LOAD, SelectionMode.FILES_ONLY );
	// 		if ( file != null )
	// 			tfLacssExecutable.setText( file.getAbsolutePath() );
	// 	}
	// 	finally
	// 	{
	// 		btnBrowseLacssPath.setEnabled( true );
	// 	}
	// }

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		// tfLacssExecutable.setText( ( String ) settings.get( KEY_LACSS_PYTHON_FILEPATH ) );
		tfCustomPath.setText( ( String ) settings.get( KEY_LACSS_CUSTOM_MODEL_FILEPATH ) );
		cmbboxPretrainedModel.setSelectedItem( settings.get( KEY_LACSS_MODEL ) );
		ftfmin_cell_area.setValue( settings.get( KEY_MIN_CELL_AREA ) );
		// chckbx_return_label.setSelected( ( boolean ) settings.get( KEY_RETURN_LABEL ) );
		chckbxBounds.setSelected( ( boolean ) settings.get( KEY_REMOVE_OUT_OF_BOUNDS ) );
		ftfmin_scaling.setValue( settings.get( KEY_SCALING));
		ftfnms_iou.setValue(settings.get(KEY_NMS_IOU));
		ftfsegmentation_threshold.setValue(settings.get(KEY_SEGMENTATION_THRESHOLD));
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final HashMap< String, Object > settings = new HashMap<>( 9 );

		// settings.put( KEY_LACSS_PYTHON_FILEPATH, tfLacssExecutable.getText() );
		settings.put( KEY_LACSS_CUSTOM_MODEL_FILEPATH, tfCustomPath.getText() );
		settings.put( KEY_LACSS_MODEL, cmbboxPretrainedModel.getSelectedItem() );

		final double min_cell_area = ( ( Number ) ftfmin_cell_area.getValue() ).doubleValue();
		settings.put( KEY_MIN_CELL_AREA, min_cell_area );
		settings.put( KEY_REMOVE_OUT_OF_BOUNDS, chckbxBounds.isSelected() );
		// settings.put( KEY_RETURN_LABEL, chckbx_return_label.isSelected() );
		
		final double scaling = ( ( Number) ftfmin_scaling.getValue()).doubleValue();
		settings.put ( KEY_SCALING, scaling );
		final double nms_iou = ((Number) ftfnms_iou.getValue()).doubleValue();
		settings.put (KEY_NMS_IOU, nms_iou);
		final double segmentation_threshold = ((Number) ftfsegmentation_threshold.getValue()).doubleValue();
		settings.put (KEY_SEGMENTATION_THRESHOLD, segmentation_threshold);

		settings.put( KEY_LOGGER, logger );

		return settings;
	}

	@Override
	public void clean()
	{} 

	public enum PretrainedModel
	{
		Default("Default", ""),
		CUSTOM( "Custom", "" );

		private final String name;

		private final String path;

		PretrainedModel( final String name, final String path )
		{
			this.name = name;
			this.path = path;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public String lacssName()
		{
			return path;
		}
	}
}
