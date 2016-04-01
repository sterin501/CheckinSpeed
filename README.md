
This RIDC code to get network speed and Checkin Capacity of UCM server 

Only JDK 1.8+ works 

1.Edit config.properties

   with URL , primary file etc

2. Run the code like

. ./classpath 

java SpeedTest

3. Sample output

GetLength in bytes  317939832
GetLength in KB 310488.0
GetLength in MB 303.2109375
Starting Uploadler
Sending File to UCM -----> 

hostname is 10.184.36.144
Sec is myrealm
Time out value is 780000
@Properties LocalData
UserDateFormat=iso8601
IdcService=CHECKIN_UNIVERSAL
UserTimeZone=UTC
dDocTitle=Test RIDC Checkin 310488.0
dDocType=Document
dDocAccount=
dSecurityGroup=Public
@end
----->15----->----->52----->----->88-----> 
 ContentID is STJACOBPC1IDCO001704
Time took in UCM server is   3.547 sec
Processing Capacity of your environment 87535.38201296871 KB/s
Processing Capacity of your environment 87.53538201296871 MB/s
100% 

time took only for file transfer is  in sec 3
Network Speed between client & UCM server KB/sec 103496.0
Network Speed between client & UCM server MB/sec 101.0703125




