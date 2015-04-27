package com.group9.getmethere;

import java.util.List;
import java.util.ArrayList;

public class polylineCodec {

    private static final char ESCAPE    =   0x5C;

    public String pointsEncode( double [] points ) {
        String output = "";
        double lat = 0, lon = 0;
        int idx = 0, len = points.length;
        do {
            output += pointEncode( points[ idx ] - lat ) + pointEncode( points[ idx + 1 ] - lon );
            lat = points[ idx ];
            lon = points[ idx + 1 ];
            idx += 2;
        } while( idx < len - 1 );
        
        return output;
    }

    public String pointEncode( double point ) {
        String output = "";
        char c;
        int chunk, decimal = (int) Math.round( point * 1e5 );
        decimal = ( decimal >= 0 ? decimal << 1 : ~( decimal << 1 ) );

//        System.out.format( "ENC: %d\n", decimal );

        do {
            chunk = decimal & 0x1f;
            decimal >>= 5;
            output += (char) ( decimal > 0x0 ? ( chunk | 0x20 ) + 63 : chunk + 63 );
        } while( decimal > 0 );

        return output;
    }

    public dataPolyLine pointsDecode( String input ) {
        String pointCode = "";
        List <Double> array = new ArrayList <Double> ();
        int idx = 0, idxArray = 0;
        boolean escapeActive = false;

        while( idx < input.length() ) {
            // Lose any escape characters
            if( input.charAt( idx ) == ESCAPE )
                idx++;
            pointCode += input.charAt( idx );
            if( ( ( input.charAt( idx ++ ) - 63 ) & 0x20 ) == 0x0 ) {
                array.add( pointDecode( pointCode ) );
                pointCode = "";
                
                if( idxArray > 2 && idxArray % 2 == 1 ) {
                    array.set( idxArray - 1, array.get( idxArray - 3 ) + array.get( idxArray - 1 ) );   
                    array.set( idxArray,     array.get( idxArray - 2 ) + array.get( idxArray     ) );
                }

                idxArray ++;
            }
        };

        idxArray = 0;
        dataPolyLine polyline = new dataPolyLine();
        // If we have any pair to process, do so!
        while( idxArray + 1 < array.size() ) {
            polyline.add( array.get( idxArray ), array.get( idxArray + 1 ) );
            idxArray += 2;
        }

        return polyline;
    }

    public double pointDecode( String code ) {
        int decimal = 0;
        for( int i = 0; i < code.length(); i++ )
            decimal += ( ( code.charAt( i ) - 63 ) & 0x1f ) << ( i * 5 );
        return ( ( decimal & 0x1 ) == 0x1 ? ( ~( decimal >> 1 ) ) * 1e-5: ( decimal >> 1 ) * 1e-5 );
    }
}

