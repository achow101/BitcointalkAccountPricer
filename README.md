# BitcointalkAccountPricer
Price Estimator for Bitcointalk Forum Accounts

##Introduction
This project is an attempt to provide accurate pricing information for the trading 
of Bitcointalk Forum Accounts. It will be updated to reflect the market frequently.

##Algorithm
Below is the algorithm used to determine the price of an account

    0.0003 btc/activity
    0.00015 btc/epa (extra potential activity)
    
    epa = potential activity - activity
    
    bp (base price) = 0.0003 * activity + 0.00015 * epa
    
    final price = bp * pqm * tm
    
    pqm (post quality multiplier) determined by ratio of good posts (>75 characters excluding quoted text) to bad posts (<75 characters excluding quotes)
    excellent post quality (90%) = 1.05 (+5%)
    good (80%) = 1.025 (+2.5%)
    fair (70%) = 1.00 (+0%)
    poor (50%) = -1.025 (-2.5%)
    very poor (<50%) = -1.05 (-5%)
    
    tm (trust multiplier)
    positive light green trust = 1.10 (+10%)
    positive dark green trust = 1.20 (+20%)
    neutral trust = 1.00 (+0%)
    negative trust = -0.15 (-85%)
    
##Installing
Installation instructiosn are coming soon. For now, you can either test this in eclipse or GWT Super Dev Mode. You can also compile and run
yourself if you know how to deploy GWT RPC code.
	
##License
Copyright (C) 2015  Andrew Chow

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

##Disclaimer
Although I strive to produce accurate estimates, this estimator is by far not definitive. It may not 
reflect what sellers are actually selling the accounts for. Furthermore, this estimator excludes specialty accounts
such as Staff, Default Trust, and Satoshi.
