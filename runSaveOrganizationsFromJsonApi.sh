mvn exec:java -Dhttps.protocols=TLSv1.3 -Dexec.mainClass="edu.ufl.bmi.misc.SaveOneObjectAtATimeFromApiProcess" -Dexec.arguments="src/main/resources/organization-api-info.txt","./organizations" -Dexec.cleanupDaemonThreads=false