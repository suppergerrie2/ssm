/**
 * Simple Stack Machine
 *
 * Written by Atze Dijkstra, atze@cs.uu.nl,
 * Copyright Utrecht University.
 *
 */

package nl.uu.cs.ssm ;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComponent;

public class Utils
{
    private static final String zeroChars = "00000000" ;
    
    private static final String hexReprPrefix = "0x" ;
    
    public static String asHex( int i, boolean hexIsDefault, boolean signed, boolean padZero )
    {
    	String sign = "" ;
    	if ( signed && i < 0 )
    	{
    		sign = "-" ;
    		i = -i ;
    	}
        String s = Integer.toHexString( i ) ;
        return
        	sign +
        	( hexIsDefault
        	? ( padZero
        	  ? zeroChars.substring( s.length() )
        	  : ""
        	  )
        	: ( i < 10
        	  ? ""
        	  : "0x"
        	  )
        	) + s ;
    }
    
    public static String asHex( int i, boolean hexIsDefault )
    {
        return asHex( i, hexIsDefault, false, true ) ;
    }
    
    public static String asHex( int i )
    {
        return asHex( i, true, false, true ) ;
    }
    
    public static <T> void addAllTo( Vector<T> v, Enumeration<T> es )
    {
    	while( es.hasMoreElements() )
    		v.addElement( es.nextElement() ) ;
    }
    
    public static <T> void addAllTo( Vector<T> v, Vector<T> es )
    {
    	addAllTo( v, es.elements() ) ;
    }
    
    public static <T> Vector<T> asVector( T[] os )
    {
    	Vector<T> v = new Vector<T>() ;
    	for ( int i = 0 ; i < os.length ; i++ )
    	    v.addElement( os[i] ) ;
    	return v ;
    }
    
    public static Vector<String> splitAt( String s, char sep )
    {
        Vector<String> v = new Vector<String>() ;
        while( true )
        {
            int i = s.indexOf( sep ) ;
            if ( i >= 0 )
            {
                v.addElement( s.substring( 0, i ) ) ;
                s = s.substring( i+1 ) ;
            }
            else
            {
                if ( s.length() > 0 )
                    v.addElement( s ) ;
                break ;
            }
        }
        return v ;
    }
    
    private static int toDigit( char c )
    {
        if (c >= '0' && c <= '9')
            return c - '0' ;
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10 ;
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10 ;
        return -1 ;
    }
    
    public static boolean isNumberRepr( String s, boolean hexIsDefault )
    {
        int radix = hexIsDefault ? 16 : 10 ;
        int pos = 0 ;
        if ( s.charAt( 0 ) == '-' )
            pos += 1 ;
        if ( ( ! hexIsDefault ) && s.regionMatches( pos, hexReprPrefix, 0, 2 ) )
            pos += 2 ;
        int max = s.length() ;
        for( ; pos < max ; pos++ )
        {
        	int d = toDigit( s.charAt( pos ) ) ;
            if ( d < 0 || d >= radix )
                return false ;
        }
        return true ;
    }
    
    public static int fromHex( String s, boolean hexIsDefault )
    {
        int radix = hexIsDefault ? 16 : 10 ;
        int res = 0 ;
        int pos = 0 ;
        int sign = 1 ;
        if ( s.charAt( 0 ) == '-' )
        {
            pos += 1 ;
            sign = -1 ;
        }
        if ( ( ! hexIsDefault ) && s.regionMatches( pos, hexReprPrefix, 0, 2 ) )
        {
            pos += 2 ;
            radix = 16 ;
        }
        int max = s.length() ;
        for( ; pos < max ; pos++ )
        {
            int d = toDigit( s.charAt( pos ) ) ;
            //System.out.println( "from hex " + s + " dig " + s.charAt( pos ) + "=" + d ) ;
            if ( ( d < 0 ) || ( radix <= d ) )
                return res ;
            res = res * radix + d ;
        }
        return res * sign ;
    }
    
    public static int fromHex( String s )
    {
        return fromHex( s, true ) ;
    }
    
    public static String repeat( String s, int l )
    {
    	StringBuffer b = new StringBuffer() ;
    	for ( int i = 0 ; i < l ; i++ )
    		b.append( s ) ;
    	return b.toString() ;
    }
    
	public static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter
	{
		private Vector<Object> extensions = new Vector<Object>() ;
		
		public ExtensionFileFilter( Enumeration<Object> exts )
		{
			while ( exts.hasMoreElements() )
				addExtension( (String)exts.nextElement() ) ;
		}
		
		public ExtensionFileFilter( String exts )
		{
			this( new StringTokenizer( exts ) ) ;
		}
		
		public void addExtension( String ext )
		{
			extensions.addElement( ext ) ;
		}
		
		public boolean accept( File f )
		{
			boolean res = true ;
			if ( ! f.isDirectory() )
			{
			    String extension = Utils.getExtension( f ) ;
			    res = (extension != null) && (extensions.contains( extension )) ;
		    }
		    return res ;
		}
		
		public String getDescription()
		{
			return ppList( extensions.elements(), "", ", ", "" ) + " files" ;
		}
	}
	
	/*
     * Get the extension of a file.
     */  
    public static String getExtension( File f )
    {
        String ext = null ;
        String s = f.getName() ;
        int i = s.lastIndexOf( '.' ) ;

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring( i+1 ).toLowerCase();
        }
        return ext;
    }

    public static String withoutExtension(File f) {
        String p = f.getAbsolutePath();

        return p.substring(0, p.length() - getExtension(f).length() - 1);
    }
    
    public static String ppList( Object l[] )
    {
    	return ppList( Arrays.asList( l ) ) ;
    }
    
    public static String ppList( java.util.List<Object> l )
    {
    	return ppList( Collections.enumeration( l ), "[", ",", "]" ) ;
    }
    
    public static String ppList( Enumeration<Object> e, String o, String s, String c )
    {
    	StringBuffer b = new StringBuffer() ;
    	b.append( o ) ;
    	if ( e.hasMoreElements() )
    	{
    		b.append( e.nextElement().toString() ) ;
    		while ( e.hasMoreElements() )
    		{
    			b.append( s ) ;
	    		b.append( e.nextElement().toString() ) ;
    		}
    	}
    	b.append( c ) ;
    	return b.toString() ;
    }
    
    public static int indexOf( int a[], int v )
    {
    	for ( int i = 0 ; i < a.length ; i++ )
    		if ( a[i] == v )
    			return v ;
    	return -1 ;
    }

    public static boolean contains( int a[], int v )
    {
    	return indexOf( a, v ) >= 0 ;
    }

	/**
	 * Scroll a component contained in a JScrollPane to its end
	 */
	public static void scrollComponentTo( JComponent c, Rectangle r )
	{
		int extraHeight = 40 ; // 2*r.height ;
		Rectangle rr = new Rectangle( r.x, r.y-extraHeight, r.width, 2*extraHeight+r.height ) ;
		Rectangle rrr = rr.intersection( c.getBounds() ) ;
		//System.out.println( "rr=" + rr + ", rrr=" + rrr + ", bounds=" + c.getBounds() ) ;
		if ( rrr.height > 0 )
			c.scrollRectToVisible( rrr ) ;
	}
	
	/**
	 * Scroll a component contained in a JScrollPane to its end
	 */
	public static void scrollComponentToEnd( JComponent c )
	{
		Rectangle r = c.getBounds() ;
		scrollComponentTo( c, new Rectangle( 0, r.height-2, r.width, 2 ) ) ;
	}

    /**
     * Convert unicode code point to String
     */
    public static String codePointToString(int n) throws UnsupportedEncodingException {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(n);
        return new String(b.array(), "UTF-32BE");
    }

    public static String readFile(Path path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }
}
