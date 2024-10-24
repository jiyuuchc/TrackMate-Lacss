package fiji.plugin.trackmate.detector.lacss;

import java.awt.Image;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.action.IJRoiExporter;
import ij.ImagePlus;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

public class LacssUtils 
{
	public static URL getResource( final String name )
	{
		return LacssDetectorFactory.class.getResource( name );
	}

	public static final ImageIcon logo()
	{
		URL icon = getResource( "/images/mimilogo.jpg" );
		if (icon == null) {
			return null;
		}
		else {
			return new ImageIcon( icon );
		}
	}

	public static final ImageIcon logo64()
	{
		ImageIcon icon = logo();
		if (icon != null)
			return scaleImage( logo(), 64, 64 );
		else 
			return null;
	}

	public static final ImageIcon scaleImage( final ImageIcon icon, final int w, final int h )
	{
		int nw = icon.getIconWidth();
		int nh = icon.getIconHeight();

		if ( icon.getIconWidth() > w )
		{
			nw = w;
			nh = ( nw * icon.getIconHeight() ) / icon.getIconWidth();
		}

		if ( nh > h )
		{
			nh = h;
			nw = ( icon.getIconWidth() * nh ) / icon.getIconHeight();
		}

		return new ImageIcon( icon.getImage().getScaledInstance( nw, nh, Image.SCALE_DEFAULT ) );
	}

	public static final Interval getCurrentFrameInterval( final ImgPlus< ? > img, final Settings settings )
	{
		final long[] max = new long[ img.numDimensions() ];
		final long[] min = new long[ img.numDimensions() ];

		// X, we must have it.
		final int xindex = img.dimensionIndex( Axes.X );
		min[ xindex ] = settings.getXstart();
		max[ xindex ] = settings.getXend();

		// Y, we must have it.
		final int yindex = img.dimensionIndex( Axes.Y );
		min[ yindex ] = settings.getYstart();
		max[ yindex ] = settings.getYend();

		// Z, we MIGHT have it.
		final int zindex = img.dimensionIndex( Axes.Z );
		if ( zindex >= 0 )
		{
			min[ zindex ] = settings.zstart;
			max[ zindex ] = settings.zend;
		}

		// Time select current frame
		final int tindex = img.dimensionIndex( Axes.TIME );
		if ( tindex >= 0 )
		{
			int frame = settings.imp.getFrame();
			min[ tindex ] = frame;
			max[ tindex ] = frame;
		}

		// CHANNEL 
		final int cindex = img.dimensionIndex( Axes.CHANNEL );
		if ( cindex >= 0 )
		{
			long n_ch = img.dimension(cindex);
			if ( n_ch <= 3 ) {
				min[ cindex ] = 0;
				max[ cindex ] = n_ch - 1;
			}
			else {
				min[ cindex ] = settings.imp.getChannel();
				max[ cindex ] = settings.imp.getChannel();
			}
		}

		final FinalInterval interval = new FinalInterval( min, max );
		return interval;
	}
  
	public static final void spotsToRois( ImagePlus imp, List<Spot> spots ) {
		IJRoiExporter exporter = new IJRoiExporter(imp, Logger.IJ_LOGGER);
		exporter.export(spots);
	}

}


