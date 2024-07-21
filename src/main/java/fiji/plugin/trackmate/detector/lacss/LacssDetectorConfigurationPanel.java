package fiji.plugin.trackmate.detector.lacss;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.DetectionPreview;

public class LacssDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final String TITLE = LacssDetectorFactory.NAME;

	private static final ImageIcon ICON = LacssUtils.logo64();

	private static final NumberFormat MIN_CELL_AREA_FORMAT = new DecimalFormat( "#.##" );

	private static final NumberFormat MIN_SCALING_FORMAT = new DecimalFormat( "#.##" );

	private static final NumberFormat MIN_NMS_IOU_FORMAT = new DecimalFormat( "#.##" );

	private static final NumberFormat MIN_SEGMENTATION_THRESHOLD_FORMAT = new DecimalFormat( "#.##" );

	protected static final String DOC1_URL = "https://jiyuuchc.github.io/lacss/api/deploy/#lacss.deploy.Predictor";

	// private final JComboBox< PretrainedModel > cmbboxPretrainedModel;

	private final JFormattedTextField ftfmin_cell_area;

	private final JFormattedTextField ftfmin_scaling;

	private final JFormattedTextField ftfnms_iou;

	private final JFormattedTextField ftfsegmentation_threshold;

	private final Logger logger;

	private final JCheckBox chckbx_multi_channel;

	// private final JTextField tfCustomPath;

	private final JTextField tfRemoteServer;

	private final JTextField tfRemoteServerToken;

	// private final JButton btnBrowseCustomModel;

	public LacssDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		this.logger = model.getLogger();
		int row = 0;

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., .1 };
		gridBagLayout.columnWidths = new int[] { 144, 0, 32 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0 };
		setLayout( gridBagLayout );

		// ICON
		final JLabel lblDetector = new JLabel( TITLE, ICON, JLabel.RIGHT );
		final GridBagConstraints gbcLblDetector = new GridBagConstraints();
		lblDetector.setFont( BIG_FONT );
		lblDetector.setHorizontalAlignment( SwingConstants.CENTER );
		gbcLblDetector.gridwidth = 3;
		gbcLblDetector.insets = new Insets( 0, 5, 5, 0 );
		gbcLblDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDetector.gridx = 0;
		gbcLblDetector.gridy = row++;
		add( lblDetector, gbcLblDetector );

		// doc link
		final String text = "Click here for the documentation";
		final JLabel lblUrl = new JLabel( text );
		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
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
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.gridwidth = 3;
		gbcLblUrl.insets = new Insets( 0, 10, 5, 15 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = row++;
		add( lblUrl, gbcLblUrl );

		// Model source
		// final JLabel lblPretrainedModel = new JLabel( "Pretrained model:" );
		// final GridBagConstraints gbcLblPretrainedModel = new GridBagConstraints();
		// lblPretrainedModel.setFont( SMALL_FONT );
		// gbcLblPretrainedModel.anchor = GridBagConstraints.CENTER;
		// gbcLblPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		// gbcLblPretrainedModel.gridx = 0;
		// gbcLblPretrainedModel.gridy = row;
		// add( lblPretrainedModel, gbcLblPretrainedModel );

		// cmbboxPretrainedModel = new JComboBox<>( new Vector<>( Arrays.asList( PretrainedModel.values() ) ) );
		// cmbboxPretrainedModel.setFont( SMALL_FONT );
		// final GridBagConstraints gbcCmbboxPretrainedModel = new GridBagConstraints();
		// gbcCmbboxPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		// gbcCmbboxPretrainedModel.fill = GridBagConstraints.HORIZONTAL;
		// gbcCmbboxPretrainedModel.gridx = 1;
		// gbcCmbboxPretrainedModel.gridy = row++;
		// add( cmbboxPretrainedModel, gbcCmbboxPretrainedModel );

		// custom model path
		// final JLabel lblPathToCustomModel = new JLabel( "Path:" );
		// final GridBagConstraints gbcLblPathToCustomModel = new GridBagConstraints();
		// lblPathToCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		// gbcLblPathToCustomModel.anchor = GridBagConstraints.CENTER;
		// gbcLblPathToCustomModel.insets = new Insets( 0, 5, 5, 5 );
		// gbcLblPathToCustomModel.gridx = 0;
		// gbcLblPathToCustomModel.gridy = row;
		// add( lblPathToCustomModel, gbcLblPathToCustomModel );

		// tfCustomPath = new JTextField( " " );
		// tfCustomPath.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		// tfCustomPath.setColumns( 15 );
		// final GridBagConstraints gbcTfCustomPath = new GridBagConstraints();
		// gbcTfCustomPath.insets = new Insets( 0, 5, 5, 5 );
		// gbcTfCustomPath.fill = GridBagConstraints.BOTH;
		// gbcTfCustomPath.gridx = 1;
		// gbcTfCustomPath.gridy = row;
		// add( tfCustomPath, gbcTfCustomPath );

		// btnBrowseCustomModel = new JButton( "Browse" );
		// btnBrowseCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		// final GridBagConstraints gbcBtnBrowseCustomModel = new GridBagConstraints();
		// gbcBtnBrowseCustomModel.insets = new Insets( 0, 0, 5, 5 );
		// gbcBtnBrowseCustomModel.anchor = GridBagConstraints.SOUTHEAST;
		// gbcBtnBrowseCustomModel.gridx = 2;
		// gbcBtnBrowseCustomModel.gridy = row++;
		// btnBrowseCustomModel.addActionListener( l -> browseCustomModelPath() );
		// add( btnBrowseCustomModel, gbcBtnBrowseCustomModel );

		// server
		final JLabel lblRemoteServer = new JLabel( "Server:" );
		lblRemoteServer.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblRemoteServer = new GridBagConstraints();
		gbcLblRemoteServer.anchor = GridBagConstraints.CENTER;
		gbcLblRemoteServer.insets = new Insets( 0, 5, 5, 5 );
		gbcLblRemoteServer.gridx = 0;
		gbcLblRemoteServer.gridy = row;
		add( lblRemoteServer, gbcLblRemoteServer );

		tfRemoteServer = new JTextField( " " );
		tfRemoteServer.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		tfRemoteServer.setColumns( 15 );
		final GridBagConstraints gbcTfRemoteServer = new GridBagConstraints();
		gbcTfRemoteServer.insets = new Insets( 0, 5, 5, 5 );
		gbcTfRemoteServer.fill = GridBagConstraints.BOTH;
		gbcTfRemoteServer.gridx = 1;
		gbcTfRemoteServer.gridy = row++;
		add( tfRemoteServer, gbcTfRemoteServer );

		final JLabel lblRemoteServerToken = new JLabel( "Access token:" );
		final GridBagConstraints gbcLblRemoteServerToken = new GridBagConstraints();
		lblRemoteServerToken.setFont( SMALL_FONT );
		gbcLblRemoteServerToken.anchor = GridBagConstraints.CENTER;
		gbcLblRemoteServerToken.insets = new Insets( 0, 5, 5, 5 );
		gbcLblRemoteServerToken.gridx = 0;
		gbcLblRemoteServerToken.gridy = row;
		add( lblRemoteServerToken, gbcLblRemoteServerToken );
 
		tfRemoteServerToken = new JTextField( " " );
		tfRemoteServerToken.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		tfRemoteServerToken.setColumns( 15 );
		final GridBagConstraints gbcTfRemoteServerToken = new GridBagConstraints();
		gbcTfRemoteServerToken.insets = new Insets( 0, 5, 5, 5 );
		gbcTfRemoteServerToken.fill = GridBagConstraints.BOTH;
		gbcTfRemoteServerToken.gridx = 1;
		gbcTfRemoteServerToken.gridy = row++;
		add( tfRemoteServerToken, gbcTfRemoteServerToken );

		// final ItemListener srcComboBoxListener = e -> {
		// 	final boolean isCustom = cmbboxPretrainedModel.getSelectedItem() == PretrainedModel.CUSTOM;
		// 	final boolean isRemote = cmbboxPretrainedModel.getSelectedItem() == PretrainedModel.Remote;
		// 	tfCustomPath.setVisible( isCustom );
		// 	lblPathToCustomModel.setVisible( isCustom );
		// 	btnBrowseCustomModel.setVisible( isCustom );
		// 	lblRemoteServer.setVisible( isRemote );
		// 	tfRemoteServer.setVisible( isRemote );
		// 	lblRemoteServerToken.setVisible( isRemote );
		// 	tfRemoteServerToken.setVisible( isRemote );
		// };
		// cmbboxPretrainedModel.addItemListener( srcComboBoxListener );
		// srcComboBoxListener.itemStateChanged( null );

		// min cell area
		final JLabel lblMin_cell_area = new JLabel( "Minimum Cell Area:" );
		lblMin_cell_area.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblMin_cell_area = new GridBagConstraints();
		gbcLblMin_cell_area.anchor = GridBagConstraints.CENTER;
		gbcLblMin_cell_area.insets = new Insets( 0, 5, 5, 5 );
		gbcLblMin_cell_area.gridx = 0;
		gbcLblMin_cell_area.gridy = row;
		add( lblMin_cell_area, gbcLblMin_cell_area );

		ftfmin_cell_area = new JFormattedTextField( MIN_CELL_AREA_FORMAT );
		ftfmin_cell_area.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfmin_cell_area = new GridBagConstraints();
		gbcFtfmin_cell_area.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfmin_cell_area.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfmin_cell_area.gridx = 1;
		gbcFtfmin_cell_area.gridy = row++;
		add( ftfmin_cell_area, gbcFtfmin_cell_area );

		// Scaling Factor
		final JLabel lblMin_scaling = new JLabel( "Scaling Factor:" );
		lblMin_scaling.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblMin_scaling = new GridBagConstraints();
		gbcLblMin_scaling.anchor = GridBagConstraints.CENTER;
		gbcLblMin_scaling.insets = new Insets( 0, 5, 5, 5 );
		gbcLblMin_scaling.gridx = 0;
		gbcLblMin_scaling.gridy = row;
		add( lblMin_scaling, gbcLblMin_scaling );

		ftfmin_scaling = new JFormattedTextField( MIN_SCALING_FORMAT );
		// ftfmin_scaling.setHorizontalAlignment( SwingConstants.CENTER );
		ftfmin_scaling.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfmin_scaling = new GridBagConstraints();
		gbcFtfmin_scaling.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfmin_scaling.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfmin_scaling.gridx = 1;
		gbcFtfmin_scaling.gridy = row++;
		add( ftfmin_scaling, gbcFtfmin_scaling );

		/* 	The minimal prediction scores.: Default : 0.5 */
		final JLabel lblsegmentation_threshold = new JLabel( "Score Treshold:" );
		lblsegmentation_threshold.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentation_threshold = new GridBagConstraints();
		gbcLblSegmentation_threshold.anchor = GridBagConstraints.CENTER;
		gbcLblSegmentation_threshold.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentation_threshold.gridx = 0;
		gbcLblSegmentation_threshold.gridy = row;
		add( lblsegmentation_threshold, gbcLblSegmentation_threshold );

		ftfsegmentation_threshold = new JFormattedTextField( MIN_SEGMENTATION_THRESHOLD_FORMAT );
		// ftfsegmentation_threshold.setHorizontalAlignment( SwingConstants.CENTER );
		ftfsegmentation_threshold.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfsegmentation_threshold = new GridBagConstraints();
		gbcFtfsegmentation_threshold.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfsegmentation_threshold.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfsegmentation_threshold.gridx = 1;
		gbcFtfsegmentation_threshold.gridy = row++;
		add( ftfsegmentation_threshold, gbcFtfsegmentation_threshold );

		/* nms_iou: Optional iou threshold for the non-max-suppression post-processing. Default is 0, which disable non-max-suppression.  */
		final JLabel lblnms_iou = new JLabel( "NMS IOU:" );
		final GridBagConstraints gbcLblNms_iou = new GridBagConstraints();
		lblnms_iou.setFont( SMALL_FONT );
		gbcLblNms_iou.anchor = GridBagConstraints.CENTER;
		gbcLblNms_iou.insets = new Insets( 0, 5, 5, 5 );
		gbcLblNms_iou.gridx = 0;
		gbcLblNms_iou.gridy = row;
		add( lblnms_iou, gbcLblNms_iou );

		ftfnms_iou = new JFormattedTextField( MIN_NMS_IOU_FORMAT );
		// ftfnms_iou.setHorizontalAlignment( SwingConstants.CENTER );
		ftfnms_iou.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfnms_iou = new GridBagConstraints();
		gbcFtfnms_iou.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfnms_iou.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfnms_iou.gridx = 1;
		gbcFtfnms_iou.gridy = row++;
		add( ftfnms_iou, gbcFtfnms_iou );
		
		// multi-channel checkbox
		final JLabel textlabel_checkbox_multi_channel = new JLabel( "Multi-Channel:" );
		textlabel_checkbox_multi_channel.setFont( SMALL_FONT );
		final GridBagConstraints gbctxtlabel_checkbox_multi_channel = new GridBagConstraints();
		gbctxtlabel_checkbox_multi_channel.anchor = GridBagConstraints.CENTER;
		gbctxtlabel_checkbox_multi_channel.insets = new Insets( 0, 5, 5, 5 );
		gbctxtlabel_checkbox_multi_channel.gridx = 0;
		gbctxtlabel_checkbox_multi_channel.gridy = row;
		add( textlabel_checkbox_multi_channel, gbctxtlabel_checkbox_multi_channel);

		chckbx_multi_channel = new JCheckBox("");
		chckbx_multi_channel.setHorizontalAlignment( SwingConstants.LEFT );
		chckbx_multi_channel.setFont( SMALL_FONT );
		final GridBagConstraints gbcchckbx_multi_channel = new GridBagConstraints();
		gbcchckbx_multi_channel.insets = new Insets( 0, 5, 0, 0 );
		gbcchckbx_multi_channel.gridx = 1;
		gbcchckbx_multi_channel.gridy = row++;
		add( chckbx_multi_channel, gbcchckbx_multi_channel );

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

	}

	// protected void browseCustomModelPath()
	// {
	// 	btnBrowseCustomModel.setEnabled( false );
	// 	try
	// 	{
	// 		final File file = FileChooser.chooseFile( this, tfCustomPath.getText(), null,
	// 				"Browse to a Lacss custom model", DialogType.LOAD, SelectionMode.FILES_ONLY );
	// 		if ( file != null )
	// 			tfCustomPath.setText( file.getAbsolutePath() );
	// 	}
	// 	finally
	// 	{
	// 		btnBrowseCustomModel.setEnabled( true );
	// 	}
	// }

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		// tfCustomPath.setText( ( String ) settings.get( Constants.KEY_LACSS_CUSTOM_MODEL_FILEPATH ) );
		tfRemoteServer.setText( ( String ) settings.get( Constants.KEY_LACSS_REMOTE_SERVER ) );
		tfRemoteServerToken.setText( ( String ) settings.get( Constants.KEY_LACSS_REMOTE_SERVER_TOKEN ) );
		// cmbboxPretrainedModel.setSelectedItem( settings.get( Constants.KEY_LACSS_MODEL ) );
		ftfmin_cell_area.setValue( settings.get( Constants.KEY_MIN_CELL_AREA ) );
		chckbx_multi_channel.setSelected( (boolean) settings.get( Constants.KEY_MULTI_CHANNEL ));
		ftfmin_scaling.setValue( settings.get( Constants.KEY_SCALING));
		ftfnms_iou.setValue( settings.get( Constants.KEY_NMS_IOU ) );
		ftfsegmentation_threshold.setValue( settings.get ( Constants.KEY_DETECTION_THRESHOLD ) );
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final Map< String, Object > settings = (new LacssDetectorFactory<>()).getDefaultSettings();

		// settings.put( Constants.KEY_LACSS_MODEL, cmbboxPretrainedModel.getSelectedItem() );
		// settings.put( Constants.KEY_LACSS_CUSTOM_MODEL_FILEPATH, tfCustomPath.getText() );
		settings.put( Constants.KEY_LACSS_REMOTE_SERVER, tfRemoteServer.getText() );
		settings.put( Constants.KEY_LACSS_REMOTE_SERVER_TOKEN, tfRemoteServerToken.getText() );
		final double min_cell_area = ( ( Number ) ftfmin_cell_area.getValue() ).doubleValue();
		settings.put( Constants.KEY_MIN_CELL_AREA, min_cell_area );
		settings.put( Constants.KEY_MULTI_CHANNEL, chckbx_multi_channel.isSelected() );		
		final double scaling = ( ( Number) ftfmin_scaling.getValue()).doubleValue();
		settings.put ( Constants.KEY_SCALING, scaling );
		final double nms_iou = ((Number) ftfnms_iou.getValue()).doubleValue();
		settings.put ( Constants.KEY_NMS_IOU, nms_iou );
		final double threshold = ((Number) ftfsegmentation_threshold.getValue()).doubleValue();
		settings.put ( Constants.KEY_DETECTION_THRESHOLD, threshold);

		settings.put( Constants.KEY_LOGGER, logger );

		return settings;
	}

	@Override
	public void clean()
	{} 

// 	public enum PretrainedModel
// 	{
// 		Default("Default", ""),
// 		CUSTOM( "Custom", "" ),
// 		Remote("Remote", "");

// 		private final String name;

// 		private final String path;

// 		PretrainedModel( final String name, final String path )
// 		{
// 			this.name = name;
// 			this.path = path;
// 		}

// 		@Override
// 		public String toString()
// 		{
// 			return name;
// 		}

// 		public String lacssName()
// 		{
// 			return path;
// 		}
// 	}
}
