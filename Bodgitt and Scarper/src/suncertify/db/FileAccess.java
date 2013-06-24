package suncertify.db;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileAccess {
	
	 private static final String databaseName = "db-2x3.db";
	 private static RandomAccessFile database = null;
	 
	 
	 //Start of data file
	 private static final int MAGIC_COOKIE_BYTES = 4;
	 private static final int NUMBER_OF_FIELDS_BYTES = 2;
	 
	 //for each field
	 private static final int FIELD_LENGTH_BYTES = 1;
	 private static final int FIELD_NAME_BYTES = 1;  
	 
	 //data section
	 private static final int RECORD_FLAG_BYTES = 1; 
	 private static final int VALID = 00; 
	 private static final int INVALID = 0xFF;
	 private static final String ENCODING = "US-ASCII";
	 
	 /**Set the individual lengths of the field record*/
	 static int[] FIELD_LENGTHS = {Subcontractor.name_Length,
    		 Subcontractor.location_Length,
    		 Subcontractor.specialties_Length,
    		 Subcontractor.size_Length,
    		 Subcontractor.rate_Length,
    		 Subcontractor.owner_Length}; 
	 

	final static int fullRecordSize = Subcontractor.entry_Length + RECORD_FLAG_BYTES;

	 
	 public static void FileAccess() throws IOException, RecordNotFoundException{
 
		 String Location = "C:\\Users\\garvey\\Desktop\\";
		 connectToDB(Location);
		 //getAllRecords();
		 //read(13);
		 String[] newRec = new String[6];
		 newRec[0] = "Other Realms";
		 newRec[1] = "Cork";
		 newRec[2] = "comics";
		 newRec[3] = "4";
		 newRec[4] = "$80.00";
		 newRec[5] = "485";
		 
		 String [] criteria = new String[2];
		 criteria[0] = "pip";
		 criteria[1] = "";


		 find(criteria);
		 //update(7,newRec);
		// read(13);
		// delete(7);
	 }
	 
	 private static void connectToDB(String dbLocation) throws IOException{
		 System.out.println("Connecting.....");
		 database = new RandomAccessFile(dbLocation + databaseName, "rw");	
		 System.out.println("Connected!");
		 database.seek(0);
	 }
	 
	 
	 private static int getInitialOffset() throws IOException{
		 database.seek(0);
		 //Read the start of the file as per the Data File Format
		 final byte [] magicCookieByteArray = new byte[MAGIC_COOKIE_BYTES];    
         final byte [] numberOfFieldsByteArray = new byte[NUMBER_OF_FIELDS_BYTES];  
         database.read(magicCookieByteArray);    
         database.read(numberOfFieldsByteArray);  
     
                  
		 /** The bytes that store the length of each field name    */
         final String [] fieldNames = new String[Subcontractor.number_Of_Fields];
         final int [] fieldLengths = new int[Subcontractor.number_Of_Fields];  
		    
         /** Get the value of the field name titles   */   
		 for (int i = 0; i < Subcontractor.number_Of_Fields; i++) {  
             final byte nameLengthByteArray[] = new byte[FIELD_NAME_BYTES];  
             database.read(nameLengthByteArray);  
             final int nameLength = getValue(nameLengthByteArray);  

             final byte [] fieldNameByteArray = new byte[nameLength];  
             database.read(fieldNameByteArray);  
             fieldNames[i] = new String(fieldNameByteArray, ENCODING);  
             
             final byte [] fieldLength = new byte[FIELD_LENGTH_BYTES];  
             database.read(fieldLength);  
             fieldLengths[i] = getValue(fieldLength);  
         } 
		 /**Setting the initial_offset to point at the begining of the first record*/
		 int initial_offset = (int) database.getFilePointer();
	     //final byte[] initial_offset = new byte[(int) database.getFilePointer()];
	     return initial_offset;
	 }
	 
	 
	 public static String[] read(int recNo) throws IOException{
		 recNo--; //offsets the zero value
		 final int offset = getInitialOffset();
		 int recordLocation = offset + (fullRecordSize*recNo);
		 database.seek(recordLocation);
		 String record[] = getSingleRecord();
		 
		 for (int i = 0; i < Subcontractor.number_Of_Fields; i++){
			 //System.out.println("-->"+record[i]);
		 }
		return record;
	 }
	 
	 
	 private static String[] getSingleRecord() throws IOException{
		          
         String[] recordValues = new String[Subcontractor.number_Of_Fields]; 
         final byte [] flagByteArray = new byte[RECORD_FLAG_BYTES];
 	    database.read(flagByteArray);
  	    final int flag = getValue(flagByteArray);
      	if (flag == VALID) {  
          	//System.out.println( "valid record");  
        } else{  
         // System.out.println("deleted record");  
        }  

        for (int i = 0; i < recordValues.length; i++) {  
        	byte[] bytes = new byte[FIELD_LENGTHS[i]];  
        	database.read(bytes);  
            recordValues[i] = new String(bytes, ENCODING);             
        }
        return recordValues;
		 
	 }
	 
	 
	 private static int getAllRecords() throws IOException{ 
         final int offset = getInitialOffset();		
		 database.seek(offset);
		 int numberOfRecords = 0;
         /** Get the value of each field in the record  */
         while (database.getFilePointer() < database.length()) {
	            String record[] = getSingleRecord();
	            numberOfRecords++;
	           // System.out.println("\n-----Record Num:" +numberOfRecords+"-----" + database.getFilePointer());
	            for (int i = 0; i < Subcontractor.number_Of_Fields; i++){
	   			 //System.out.println("-->"+record[i]);
	   		 	 }
	        }
         
         return numberOfRecords;
       } 
	
	 
	 private static void update(int recNo, String[] data) throws IOException{
		 //get record to update
		 recNo--; //offsets the record zero value
		 final int offset = getInitialOffset();
		 int recordLocation = offset + (recNo * fullRecordSize); 
		 database.seek(recordLocation);
		 
		 byte b = VALID; //valid file byte
		 database.write(b);
		 
		 //for each field output the new value + white space
		 for(int i = 0; i < Subcontractor.number_Of_Fields; i++){
			int padding = FIELD_LENGTHS[i] - data[i].getBytes().length;
			database.write(data[i].getBytes());
			while(padding != 0){
				database.write(' ');
				padding --;
			}
		 }
	}
	 
	 
	 private static void delete(int recNo) throws RecordNotFoundException, IOException{
		 //get record to update
		 recNo--; //offsets the record zero value
		 final int offset = getInitialOffset();
		 int recordLocation = offset + (recNo * fullRecordSize); 
		 database.seek(recordLocation);
		 
		 byte b = (byte) INVALID; //valid file byte
		 database.write(b);
		 int padding = Subcontractor.entry_Length;
		 while(padding != 0){
				database.write(' ');
				padding --;
		 }
		 
	 }
	 
	 
	 public static int [] find(String [] criteria) throws RecordNotFoundException, IOException{
		 //System.out.println("searching for " + criteria[0]);
		 int counter = 0;
		 int totalRecords = getAllRecords();
		 int [] matchingRecords = new int[totalRecords];
		 final int offset = getInitialOffset();
		 database.seek(offset);
		 for(int i = 1; i <= totalRecords; i++){
			 String [] record = read(i);
			 
			 if(record[0].contains(criteria[0]) && record[1].contains(criteria[1])){
				 System.out.println("Does " + record[0] + " = " + criteria[0]);
				 
				 matchingRecords[counter] = i;
				 System.out.println("\n\n" + matchingRecords[counter]);
				 counter++;
			 }
		 }

		 return matchingRecords;
	 }
	 
	 
	 private static int getValue(final byte [] byteArray) {  
         int value = 0;  
         final int byteArrayLength = byteArray.length;  
   
         for (int i = 0; i < byteArrayLength; i++) {  
             final int shift = (byteArrayLength - 1 - i) * 8;  
             value += (byteArray[i] & 0x000000FF) << shift;  
         }  
         return value;  
     } 	 
	   
}