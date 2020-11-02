# Javanaise
a manager for a cache of distributed objects in Java applications


# Execution
From out/production/Javanaise:
   java jvn/JvnCoordImpl (to start the coordinator)
   
   java irc/Irc (Irc version 1) 
   
   java irc/IrcV2 (Irc version 2)
   
   java jvn/JvnClient (Javanaise version 2 with an interactive command line interface)
   
   java tests/Burst1 (to test 1 client randomly reading and writing to an object)
   
   java tests/Burst_N_1 (to test N clients randomly reading and writing to an object)
   
It is recommended to restart the coordinator (if already started) before running a client program.
  

  
