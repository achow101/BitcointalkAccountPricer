/*
 * Copyright (C) 2016  Andrew Chow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.achow101.bctalkaccountpricer.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Address
	{
		private String addr;
		private String postURL;
		private String postDate;
		
		public Address(String addr, String postURL, String postDate)
		{
			this.addr = addr;
			this.postURL = postURL;
			this.postDate = postDate;
		}
		
		public String getAddr()
		{
			return addr;
		}
		
		public void setDateURL(String postDate, String postURL)
		{
			this.postDate = postDate;
			this.postURL = postURL;
		}

		public String toString()
		{
			return "<a href=\"" + postURL + "\">" + addr + " First posted on: " + postDate + "</a>";
		}
		
		public boolean isValid()
		{
			return decodeChecked(addr);
		}

		// Below borrowed and modified from Bitcoinj
		public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
		private static final char ENCODED_ZERO = ALPHABET[0];
		private static final int[] INDEXES = new int[128];
		static {
			Arrays.fill(INDEXES, -1);
			for (int i = 0; i < ALPHABET.length; i++) {
				INDEXES[ALPHABET[i]] = i;
			}
		}
		/**
		 * Decodes the given base58 string into the original data bytes, using the checksum in the
		 * last 4 bytes of the decoded data to verify that the rest are correct. The checksum is
		 * removed from the returned data.
		 *
		 * @param input the base58-encoded string to decode (which should include the checksum)
		 */
		public static boolean decodeChecked(String input) {
			byte[] decoded  = decode(input);
			if (decoded == null || decoded.length < 4)
				return false;
			byte[] data = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
			byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
			byte[] actualChecksum = Arrays.copyOfRange(hashTwice(data), 0, 4);
			if (!Arrays.equals(checksum, actualChecksum))
				return false;
			return true;
		}

		private static byte[] hashTwice(byte[] data) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(data);
				byte[] hash1 = md.digest();
				MessageDigest md2 = MessageDigest.getInstance("SHA-256");
				md2.update(hash1);
				return md2.digest();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Decodes the given base58 string into the original data bytes.
		 *
		 * @param input the base58-encoded string to decode
		 * @return the decoded data bytes
		 */
		public static byte[] decode(String input) {
			if (input.length() == 0) {
				return new byte[0];
			}
			// Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
			byte[] input58 = new byte[input.length()];
			for (int i = 0; i < input.length(); ++i) {
				char c = input.charAt(i);
				int digit = c < 128 ? INDEXES[c] : -1;
				if (digit < 0) {
					return null;
				}
				input58[i] = (byte) digit;
			}
			// Count leading zeros.
			int zeros = 0;
			while (zeros < input58.length && input58[zeros] == 0) {
				++zeros;
			}
			// Convert base-58 digits to base-256 digits.
			byte[] decoded = new byte[input.length()];
			int outputStart = decoded.length;
			for (int inputStart = zeros; inputStart < input58.length; ) {
				decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
				if (input58[inputStart] == 0) {
					++inputStart; // optimization - skip leading zeros
				}
			}
			// Ignore extra leading zeroes that were added during the calculation.
			while (outputStart < decoded.length && decoded[outputStart] == 0) {
				++outputStart;
			}
			// Return decoded data (including original number of leading zeros).
			return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
		}



		/**
		 * Divides a number, represented as an array of bytes each containing a single digit
		 * in the specified base, by the given divisor. The given number is modified in-place
		 * to contain the quotient, and the return value is the remainder.
		 *
		 * @param number the number to divide
		 * @param firstDigit the index within the array of the first non-zero digit
		 *        (this is used for optimization by skipping the leading zeros)
		 * @param base the base in which the number's digits are represented (up to 256)
		 * @param divisor the number to divide by (up to 256)
		 * @return the remainder of the division operation
		 */
		private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
			// this is just long division which accounts for the base of the input digits
			int remainder = 0;
			for (int i = firstDigit; i < number.length; i++) {
				int digit = (int) number[i] & 0xFF;
				int temp = remainder * base + digit;
				number[i] = (byte) (temp / divisor);
				remainder = temp % divisor;
			}
			return (byte) remainder;
		}

	}
