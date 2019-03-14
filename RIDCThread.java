import java.io.*;
import oracle.stellent.ridc.*;
import oracle.stellent.ridc.model.*;
import oracle.stellent.ridc.protocol.*;
import oracle.stellent.ridc.protocol.intradoc.*;
import oracle.stellent.ridc.common.log.*;
import oracle.stellent.ridc.model.serialize.*;
import oracle.stellent.ridc.protocol.http.*;
import java.util.*;
import java.text.DecimalFormat;
/*
 * @author Matt Rudd - Oracle Inc
 *
 * This is a class used to test the basic functionality
 * of submitting a checkin to Content Server using RIDC.
 */

public class RIDCThread {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create a new IdcClientManager
		IdcClientManager manager = new IdcClientManager ();
		                  	Properties prop = new Properties();
	                InputStream input = null;

    List<SubmitToUCM> clients = new ArrayList<SubmitToUCM>();


		try{


               input = new FileInputStream("config.properties");

		// load a properties file
		prop.load(input);

      String URL = prop.getProperty("url");
       // Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
			IdcClient idcClient = manager.createClient (URL);

                       IdcContext userContext = new IdcContext (prop.getProperty("user"),prop.getProperty("password"));

			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());

			// Create a new binder for submitting a search
      File[] files = new File(prop.getProperty("FolderToUpload")).listFiles();
			int count =0;
		  long startTimeS = System.currentTimeMillis();

			  for (File file : files) {

					if (file.isDirectory()) {
                continue;

					}

        else {
	           //		DataBinder dataBinder = createBinderFromFile(idcClient,prop,file);
             count++;
						 SubmitToUCM STU = new SubmitToUCM (idcClient,prop,file,userContext,count);
            serializer.serializeBinder (System.out, STU.dataBinder);
						//STU.doCheckinAndDisplayResponce();
               clients.add(STU);
							//   STU.start();

            // Send the request to Content Server

	} // end of else
}  // end Sof files loop

// Starting all threads
List<SubmitToUCM> BatchArray = new ArrayList<SubmitToUCM>();
int BatchC=Integer.parseInt(prop.getProperty("BatchSize"));
for(int k=0; k<clients.size();k++)

{

          BatchArray.add (clients.get(k));
          clients.get(k).start();


					if ( (k+1) % BatchC == 0)
					     {

											checkThreadStatus(BatchArray);
                      BatchArray.clear();
								}


				//	System.out.println(clients.get(k).isAlive());

}

// checking all threads

  checkThreadStatus(BatchArray);


	long endTimeS = System.currentTimeMillis();

	System.out.println("Time for check in  of  "+ count + " files   "  + (endTimeS - startTimeS)/1000.0 + " seconds");



			} catch (IdcClientException ice){
          ice.printStackTrace();
         }

	  catch (IOException ioe){
		ioe.printStackTrace();
	}

	} // end of main


public static void checkThreadStatus(List<SubmitToUCM>  clients)

{

	for(int k=0; k<clients.size();k++)

	{

	        if (clients.get(k).isAlive())
	           {
	                      System.out.println(clients.get(k).ThreadName +"  still uploading_______----_______ ");
	                      clients.get(k).join();


						 }
					else {

						    System.out.println(clients.get(k).ThreadName + " was able to check in  " + clients.get(k).timeRequired + " milliseconds" );
					}



	} // end of  checkThreadStatus


} // end of checkThreadStatus




} // end of class


class SubmitToUCM implements Runnable {

		private Thread t;
    private IdcClient idcClient;
		public String ThreadName;
    public String URL;
	//	public File primaryFile;
		public DataBinder dataBinder;
		private IdcContext userContext;
		public long timeRequired;

		SubmitToUCM (IdcClient idcClient,Properties prop,File primaryFile,IdcContext userContext,int count)

		{
			     this.idcClient = idcClient;
					 this.userContext = userContext;
		       this.dataBinder = idcClient.createBinder();
					 this.ThreadName = "Upload "+ count;
					 this.URL=prop.getProperty("url");

	try {


			// Databinder for checkin request

						dataBinder.putLocal("IdcService", "CHECKIN_UNIVERSAL");
					//	File primaryFile =  new File( prop.getProperty("primaryFile") );


						dataBinder.addFile("primaryFile",primaryFile);

						 List<String> skipwords = new ArrayList<String>();
								skipwords.add("url");
								skipwords.add("user");
								skipwords.add("password");
								skipwords.add("primaryFile");
								skipwords.add("FolderToUpload");


						for (String key : prop.stringPropertyNames()) {

													 boolean skip=true;
															 for(String str: skipwords) {
																		 if(str.trim().contains(key))
																					skip=false;
																												}

																												if (skip)
																			 dataBinder.putLocal(key,prop.getProperty(key));


																													}


																													String name = primaryFile.getName();
																													 this.ThreadName =  this.ThreadName+"  "+name;
																				 									int pos = name.lastIndexOf(".");
																				 									if (pos > 0) {
																				 													 name = name.substring(0, pos);
																				 																}



							if (prop.containsKey("dDocTitle"))

								 dataBinder.putLocal("dDocTitle",prop.getProperty("dDocTitle")+" "+name);
							else
								  dataBinder.putLocal("dDocTitle",name);

			 } catch (IOException ioe){
				 ioe.printStackTrace();
			 }



	 } // end of Constructor

public void run(){

	 try {

  long startTime = System.currentTimeMillis();
	ServiceResponse response = this.idcClient.sendRequest(this.userContext,this.dataBinder);
	// Get the data binder for the response from Content Server
	DataBinder responseData = response.getResponseAsBinder();
	// Write the response data binder to stdout
//  serializer.serializeBinder (System.out, responseData);

	System.out.println("_________________________________________________");
	System.out.println("ContentID   : " + responseData.getLocal("dDocName"));
	System.out.println("dID         : " + responseData.getLocal("dID"));
	System.out.println("Title       : " + responseData.getLocal("dDocTitle"));
	System.out.println("Size        : " + readableFileSize (Long.parseLong(responseData.getLocal("dFileSize"))) +"\n\n");
	String GET_FILE_URL;

	GET_FILE_URL=this.URL+"?IdcService=DOC_INFO&dID="+responseData.getLocal("dID")+"&dDocName="+responseData.getLocal("dDocName");

	System.out.println("DOC INFO    : "+ GET_FILE_URL);
	long endTime = System.currentTimeMillis();

	this.timeRequired = (endTime - startTime);
	System.out.println("Time for check in  " + this.timeRequired + " milliseconds");
	System.out.println("_________________________________________________");
   }catch (IdcClientException ice){
			ice.printStackTrace();
		 }



} // end of doCheckinAndDisplayResponce

public static String readableFileSize(long size) {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
}

public void start () {

	System.out.println("Starting "+this.ThreadName);
	      if (t == null) {
	         t = new Thread (this);
	         t.start ();
	      }


} // end of start

public boolean isAlive(){

	return t.isAlive();
} //end of isAlive

public void join(){



 try {
	 System.out.println("Waiting for  >>>>>>>>>>>>>>>>>>>>>> "+this.ThreadName);
 t.join();// Waiting for c1 to finish
    } catch (InterruptedException ie) {
          }

}  // end of join


} // end of SubmitToUCM
