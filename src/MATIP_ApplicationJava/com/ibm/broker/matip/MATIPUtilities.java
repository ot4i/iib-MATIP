package com.ibm.broker.matip;

public class MATIPUtilities {	
	
	public static int inflateBits(int[] intarray1,int[] intarray2,int[] intarray3,int[] intarray4 ) {		
		int int1 = 0;
		int int2 = 0;
		int int3 = 0;
		int int4 = 0;
		for (int i=0; i < 8; i++) {
			if (intarray1[7-i] == 1) {int1 = int1 | (0x00000001 << i);}
			if (intarray2[7-i] == 1) {int2 = int2 | (0x00000001 << i);}
			if (intarray3[7-i] == 1) {int3 = int3 | (0x00000001 << i);}
			if (intarray4[7-i] == 1) {int4 = int4 | (0x00000001 << i);}
		}		
		int1 = int1 << 24;
		int2 = int2 << 16;
		int3 = int3 << 8;
		int bytequad = (int1 | int2 | int3 | int4);
		return bytequad;
	}

	public static int[] convertResourceID(String twochars) {		
		int IDint = Integer.parseInt(twochars,16);
		int[] outputintarray = new int[8];
		for (int i=0; i < 8; i++) {			
			int compare = 0x00000001 << i;
			if ((IDint & compare) == compare) {outputintarray[7-i] = 1;}
			else {outputintarray[7-i] = 0;}
		}
		return outputintarray;
	}	
		
}
