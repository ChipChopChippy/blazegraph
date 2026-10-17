/**

Copyright (C) SYSTAP, LLC DBA Blazegraph 2006-2016.  All rights reserved.

Contact:
     SYSTAP, LLC DBA Blazegraph
     2501 Calvert ST NW #106
     Washington, DC 20008
     licenses@blazegraph.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.bigdata.rdf.internal;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.bigdata.rdf.internal.impl.literal.AbstractLiteralIV;
import com.bigdata.rdf.internal.impl.literal.UUIDLiteralIV;
import com.bigdata.rdf.model.BigdataLiteral;

/**
 * 
 * Inline URI Handler to handle URI's in the form of a Hex UUID such as:
 * 
 *  <pre>
 *   http://blazegraph.com/element/010072F0000038090100000000D56C9E
 *  </pre>
 *  
 *  {@link https://jira.blazegraph.com/browse/BLZG-1936}
 * 
 * @author beebs
 *
 */
public class InlineHexUUIDURIHandler extends InlineURIHandler {

    /**
     * Default URI namespace for inline UUIDs.
     */
    public static final String NAMESPACE = "urn:hex:uuid:";

    public InlineHexUUIDURIHandler(final String namespace) {
        super(namespace);
    }
    
    @SuppressWarnings("rawtypes")
    protected AbstractLiteralIV createInlineIV(final String localName) {
        
        if (localName == null) {
            return null;
        }
        
        try {
            final byte uuid[] = parseHexBinary(localName);
            return new UUIDLiteralIV(asUuid(uuid));
        } catch (IllegalArgumentException ex) {
            /*
             * Could not parse localName into a UUID.  Fall through to TermIV.
             */
            return null;
        }
        
    }
    
    @SuppressWarnings("rawtypes")
	@Override
	public String getLocalNameFromDelegate(
			AbstractLiteralIV<BigdataLiteral, ?> delegate) {

    	final UUID uuid = ((UUIDLiteralIV) delegate).getInlineValue();

		final String localName = printHexBinary(asBytes(uuid));
    	
		return localName;
	}
    
	public static UUID asUuid(byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long firstLong = bb.getLong();
		long secondLong = bb.getLong();
		return new UUID(firstLong, secondLong);
	}

	public static byte[] asBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	private static byte[] parseHexBinary(String value) {
		if ((value.length() & 1) != 0) {
			throw new IllegalArgumentException("Hexadecimal value must have an even length");
		}

		final byte[] bytes = new byte[value.length() / 2];
		for (int i = 0; i < value.length(); i += 2) {
			final int high = hexValue(value.charAt(i));
			final int low = hexValue(value.charAt(i + 1));
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException("Invalid hexadecimal value");
			}
			bytes[i / 2] = (byte) ((high << 4) | low);
		}
		return bytes;
	}

	private static int hexValue(char value) {
		if (value >= '0' && value <= '9') {
			return value - '0';
		}
		if (value >= 'A' && value <= 'F') {
			return value - 'A' + 10;
		}
		if (value >= 'a' && value <= 'f') {
			return value - 'a' + 10;
		}
		return -1;
	}

	private static String printHexBinary(byte[] bytes) {
		final char[] digits = "0123456789ABCDEF".toCharArray();
		final char[] value = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			value[i * 2] = digits[(bytes[i] >>> 4) & 0x0F];
			value[i * 2 + 1] = digits[bytes[i] & 0x0F];
		}
		return new String(value);
	}
    
}
