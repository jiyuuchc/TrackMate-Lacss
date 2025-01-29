package fiji.plugin.trackmate.lacss;

import fiji.plugin.trackmate.TrackMatePlugIn;
import ij.IJ;
// import ij.ImageJ;
import ij.ImagePlus;

public class LacssFullTest
{
	public static void main( final String[] args )
	{						  
		// ImageJ.main( args );
		final ImagePlus imp = IJ.openImage( "../test.tif" );
		// final ImagePlus imp = IJ.openImage( "../exp_1.tif" );
		imp.show();

		new TrackMatePlugIn().run( null );
	}
}
