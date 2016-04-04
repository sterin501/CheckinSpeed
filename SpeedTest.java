import java.io.*;
import oracle.stellent.ridc.*;
import oracle.stellent.ridc.model.*;
import oracle.stellent.ridc.protocol.*;
import oracle.stellent.ridc.protocol.intradoc.*;
import oracle.stellent.ridc.common.log.*;
import oracle.stellent.ridc.model.serialize.*;
import oracle.stellent.ridc.protocol.http.*;
import oracle.stellent.ridc.common.http.utils.RIDCHttpConstants.HttpLibrary;
import oracle.stellent.ridc.protocol.http.IdcHttpClientConfig;
import java.util.*;



class RunnableDemo implements Runnable {
   private Thread t;
   public String threadName;
   boolean statucheck=false;
   public  RIDCUploader RU;

  public  TransferFile.TransferProgress primaryProgress;
   
   RunnableDemo( String name)  {
                                  threadName = name;
                                   RU = new RIDCUploader ();

                                                
                                                primaryProgress = RU.priProgress;
                                                  statucheck=true;

            
        

                              }


   public void run() {
   
              
      try {

          
                                   
                                          

int onlyfiletransfer = 0;
                            
               
         while (true) {
                                              

                      int percent = primaryProgress.getTransferPercentage();
                                                                   
                                 
                                             if (percent==100)
                                                {   System.out.print("-->100% \n\n"); 

                                                    System.out.println("time took only for file transfer is   "+onlyfiletransfer + " sec" );   

                                                    System.out.println("Network Speed between client & UCM server is   "+RU.sizeinKB/onlyfiletransfer + " KB/sec"); 
                                                      
                                                    System.out.println("Network Speed between client & UCM server is  "+RU.sizeinKB/(1024*onlyfiletransfer) + "MB/sec" );     


                                                    break;       

                                               } 

                                             else if (percent==0)
                                                {
                                                    System.out.println("Sending File to UCM -----> \n");
                                                       onlyfiletransfer = 0;
                                                 }
                                             else
                                                {

                                                 System.out.print("----->"+percent+"%"); 
                                                 onlyfiletransfer++;
                                                 
                                                   double  speedsofar = (RU.sizeinKB * percent)/( onlyfiletransfer * 100);
                                                   System.out.print("    speed "+speedsofar+" KB/sec");

                                                   double  timerequried = RU.sizeinKB/speedsofar;

                                                  double  eta = timerequried - onlyfiletransfer;

                                                  System.out.println("   ETA   "+eta + " sec"); 
                                                  


                                                }

                                 
                                    


           
                     
                   t.sleep(1000);
              

         }


     } catch (InterruptedException e) {
         System.out.println("Thread " +  threadName + " interrupted.");
     } 




       
   }
   
   public void start ()
   {
      System.out.println("Starting " +  threadName );
      


                            


     if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
                                         if(statucheck )
                                           {
                                                RU.uploaderMethod();
                                              
                                                statucheck=false;
                                           }
                

      }
   

   }


}

public class SpeedTest {
   public static void main(String args[]) {
   
      RunnableDemo R1 = new RunnableDemo( "Uploadler");
      R1.start();
    


   }   
}





class RIDCUploader {

File file;
TransferFile TF;
TransferFile.TransferProgress priProgress ;
double sizeinKB;

public RIDCUploader () {

Properties prop = new Properties();
	                InputStream input = null;
                

try{


            input = new FileInputStream("config.properties");
 
		// load a properties file
		prop.load(input);

  file = new File ( prop.getProperty("primaryFile") );


  TF = new TransferFile (file);

 long transferlengrth = TF.getContentLength();

System.out.println("Size of file  in bytes  " + transferlengrth);

sizeinKB = transferlengrth/1024 ;
System.out.println("Size of file  in KB " + sizeinKB);
System.out.println("Size of file  in MB " + sizeinKB/(1024));

priProgress = TF.new TransferProgress(transferlengrth);
TF.setTransferListener(priProgress);
  
} catch (IOException ioe){
			ioe.printStackTrace();
		}




}


public  void uploaderMethod () {


IdcClientManager manager = new IdcClientManager ();
		                  	Properties prop = new Properties();
	                InputStream input = null;
                    
               

                    
		try{
			

               input = new FileInputStream("config.properties");
 
		// load a properties file
		prop.load(input);


       // Create a new IdcClient Connection using idc protocol (i.e. socket connection to Content Server)
                          
                            String httpLibrary = "httpurlconnection";

			IdcClient idcClient = manager.createClient (prop.getProperty("url"));

                           IdcClientConfig idcConfig = idcClient.getConfig ();
 
                           IdcHttpClientConfig config = (IdcHttpClientConfig)idcConfig; 
 
                           config.setHttpLibrary(HttpLibrary.valueOf(httpLibrary)); 


                       IdcContext userContext = new IdcContext (prop.getProperty("user"),prop.getProperty("password"));


                     System.out.println("hostname is " +        config.getHostName());

                         System.out.println("Sec is "      +     config.getSecurityRealm());

                        

                                       config.setSocketTimeout(780000);

                             System.out.println("Time out value is "+ config.getSocketTimeout());

			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Create a new binder for submitting a search
			DataBinder dataBinder = idcClient.createBinder();
			
			// Databinder for checkin request
			
            dataBinder.putLocal("IdcService", "CHECKIN_UNIVERSAL");
  
        
            dataBinder.putLocal("dDocType", "Document");
            
 
            dataBinder.putLocal("dDocAccount", "");
            dataBinder.putLocal("dSecurityGroup", "Public");


            dataBinder.addFile("primaryFile", TF );

 

                                                 

                                        long  filesize = file.length();
                                          double           filesizeinKB =  filesize/(1024);

                                        


                      dataBinder.putLocal("dDocTitle", ("Test RIDC Checkin "+ filesizeinKB));
            // Write the data binder for the request to stdout
            serializer.serializeBinder (System.out, dataBinder);
            // Send the request to Content Server
            long  currentTimeMillis =   System.currentTimeMillis();
           ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
            // Get the data binder for the response from Content Server
              DataBinder responseData = response.getResponseAsBinder();
              System.out.println(" \n ContentID is " + responseData.getLocal("dDocName"));
            
                                   double timespend =  System.currentTimeMillis() - currentTimeMillis;

                                    System.out.println("Time took in UCM server is    " + timespend/1000 + " sec");

                                   System.out.println("Processing Capacity of your environment "+ filesizeinKB*1000/timespend + " KB/s");
                                     System.out.println("Processing Capacity of your environment "+ filesizeinKB/timespend + " MB/s");

			
		} catch (IdcClientException ice){
			ice.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}


} 

} // end of RIDCUploader class
