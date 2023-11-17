package fiji.plugin.trackmate.lacss;

import java.util.Map;

import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class LacssFullTest
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{						  
		// UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		ImageJ.main( args );

		final ImagePlus imp = IJ.openImage( "../s1.tif" );
		imp.show();

		final Settings settings = new Settings( imp );
		final LacssDetectorFactory< ? > detectorFactory = new LacssDetectorFactory<>();
		final Map< String, Object > detectorSettings = detectorFactory.getDefaultSettings();
		settings.detectorFactory = detectorFactory;
		settings.detectorSettings = detectorSettings;

		final TrackMate trackMate = new TrackMate( settings );
		// labelImgTrackMate.setNumThreads( 1 );
		if ( !trackMate.execDetection() )
		{
			System.err.println(trackMate.getErrorMessage());
			return;
		}

		final SpotCollection spots = trackMate.getModel().getSpots();

		spots.setVisible( true );
		System.out.println( spots );

		final Model model = new Model();
		model.setSpots( spots, false );
		final SelectionModel selectionModel = new SelectionModel( model );
		
		final HyperStackDisplayer displayer = new HyperStackDisplayer( model, selectionModel, imp, DisplaySettingsIO.readUserDefault() );
		displayer.render();

	}
}
