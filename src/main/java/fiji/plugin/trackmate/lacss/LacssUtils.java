package fiji.plugin.trackmate.lacss;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

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

	public static final Interval getIntervalWithTime( final ImgPlus< ? > img, final Settings settings )
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

		// management to elsewhere.
		final int tindex = img.dimensionIndex( Axes.TIME );
		if ( tindex >= 0 )
		{
			min[ tindex ] = settings.tstart;
			max[ tindex ] = settings.tend;
		}

		// CHANNEL, we might have it, we drop it.
		final long[] max2;
		final long[] min2;
		final int cindex = img.dimensionIndex( Axes.CHANNEL );
		if ( cindex >= 0 )
		{
			max2 = new long[ img.numDimensions() - 1 ];
			min2 = new long[ img.numDimensions() - 1 ];
			int d2 = 0;
			for ( int d = 0; d < min.length; d++ )
			{
				if ( d != cindex )
				{
					min2[ d2 ] = min[ d ];
					max2[ d2 ] = max[ d ];
					d2++;
				}
			}
		}
		else
		{
			max2 = max;
			min2 = min;
		}

		final FinalInterval interval = new FinalInterval( min2, max2 );
		return interval;
	}
  
}
