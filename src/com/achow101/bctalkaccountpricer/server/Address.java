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
		
		public boolean isValid()
		{
			if (addr.length() < 26 || addr.length() > 35) return false;
		    byte[] decoded = DecodeBase58(addr, 58, 25);
		    if (decoded == null) return false;
		 
		    byte[] hash = Sha256(decoded, 0, 21, 2);
		 
		    return Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
		}
		
		private byte[] DecodeBase58(String input, int base, int len) {
			String alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
			
		    byte[] output = new byte[len];
		    for (int i = 0; i < input.length(); i++) {
		        char t = input.charAt(i);
		 
		        int p = alphabet.indexOf(t);
		        if (p == -1) return null;
		        for (int j = len - 1; j > 0; j--, p /= 256) {
		            p += base * (output[j] & 0xFF);
		            output[j] = (byte) (p % 256);
		        }
		        if (p != 0) return null;
		    }
		 
		    return output;
		}
		 
		private byte[] Sha256(byte[] data, int start, int len, int recursion) {
		    if (recursion == 0) return data;
		 
		    try {
		        MessageDigest md = MessageDigest.getInstance("SHA-256");
		        md.update(Arrays.copyOfRange(data, start, start + len));
		        return Sha256(md.digest(), 0, 32, recursion - 1);
		    } catch (NoSuchAlgorithmException e) {
		        return null;
		    }
		}
		
		public String toString()
		{
			return "<a href=\"" + postURL + "\">" + addr + " First posted on: " + postDate + "</a>"; 
		}
	}
