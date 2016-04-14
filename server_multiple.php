#!/usr/bin/php -q 
<?php 
/** 
  * Listens for requests and forks on each connection 
  */ 
$last_time_java;
$__server_listening = true; 

/* change this to your own host / port */ 
server_loop("192.168.0.105", 1907); 



error_reporting(E_ALL); 
set_time_limit(0); 
ob_implicit_flush(); 
declare(ticks = 1); 

become_daemon(); 

/* nobody/nogroup, change to your host's uid/gid of the non-priv user */ 
change_identity(1000, 1000); 

/* handle signals */ 
pcntl_signal(SIGTERM, 'sig_handler'); 
pcntl_signal(SIGINT, 'sig_handler'); 
pcntl_signal(SIGCHLD, 'sig_handler'); 

<<<<<<< Updated upstream

=======
/* change this to your own host / port */ 
server_loop("192.168.1.3", 1906); 
>>>>>>> Stashed changes

/** 
  * Change the identity to a non-priv user 
  */ 
function change_identity( $uid, $gid ) 
{ 
    if( !posix_setgid( $gid ) ) 
    { 
        print "Unable to setgid to " . $gid . "!\n"; 
        exit; 
    } 

    if( !posix_setuid( $uid ) ) 
    { 
        print "Unable to setuid to " . $uid . "!\n"; 
        exit; 
    } 
} 

/** 
  * Creates a server socket and listens for incoming client connections 
  * @param string $address The address to listen on 
  * @param int $port The port to listen on 
  */ 
function server_loop($address, $port) 
{ 
   
    GLOBAL $__server_listening; 

    if(($sock = socket_create(AF_INET, SOCK_STREAM, 0)) < 0) 
    { 
        echo "failed to create socket: ".socket_strerror($sock)."\n"; 
        exit(); 
    } 

    if(($ret = socket_bind($sock, $address, $port)) < 0) 
    { 
        echo "failed to bind socket: ".socket_strerror($ret)."\n"; 
        exit(); 
    } 

    if( ( $ret = socket_listen( $sock, 0 ) ) < 0 ) 
    { 
        echo "failed to listen to socket: ".socket_strerror($ret)."\n"; 
        exit(); 
    } 

    socket_set_nonblock($sock); 
    
    echo "waiting for clients to connect\n"; 

    while ($__server_listening) 
    { 
        $connection = @socket_accept($sock); 
        if ($connection === false) 
        { 
            usleep(100); 
        }elseif ($connection > 0) 
        { 
            handle_client($sock, $connection); 
        }else 
        { 
            echo "error: ".socket_strerror($connection); 
            die; 
        } 
    } 
} 

/** 
  * Signal handler 
  */ 
function sig_handler($sig) 
{ 
    switch($sig) 
    { 
        case SIGTERM: 
        case SIGINT: 
            exit(); 
        break; 

        case SIGCHLD: 
            pcntl_waitpid(-1, $status); 
        break; 
    } 
} 

/** 
  * Handle a new client connection 
  */ 
function handle_client($ssock, $csock) 
{ 
   
    $last_time_java = time();
    GLOBAL $__server_listening; 

    $pid = pcntl_fork(); 

    if ($pid == -1) 
    { 
        /* fork failed */ 
        echo "fork failure!\n"; 
        die; 
    }elseif ($pid == 0) 
    { 
        /* child process */ 
        $__server_listening = false; 
        socket_close($ssock); 
        interact($csock); 
        socket_close($csock); 
    }else 
    { 
        socket_close($csock); 
    } 
} 

function AccessDatabase($mode, $packet_seq_nr=NULL, $packet_time=NULL, $packet_node_nr=NULL)
{
		GLOBAL $last_time_java;
		$host = "db.ist.utl.pt";
		$user = "ist175847";
		$pass = "sgks0281";
		$dsn = "mysql:host=$host;dbname=$user";
		try{
			$connection = new PDO($dsn, $user, $pass);
		}
		catch(PDOException $exception){
			echo("<p>Error: ");
			echo($exception->getMessage());
			echo("</p>");
			exit();
		}
		switch($mode){
			
			case "insert":	
				$sql_insert = " INSERT INTO sensor_readings 
								VALUES ('$packet_seq_nr', '$packet_time', $packet_node_nr, false);";
				$result = $connection->query($sql_insert);
				if ($result == FALSE){
					$info = $connection->errorInfo();
					echo("Error: {$info[2]}\n");
					exit();
				}
				return 0;
				break;
	
			case "read":
				
				$sql_read = "SELECT * FROM sensor_readings WHERE tstamp IN (SELECT MAX(tstamp) FROM sensor_readings);";
				$read_result = $connection->query($sql_read);
				if ($read_result == FALSE){
					$info = $connection->errorInfo();
					echo("Error: {$info[2]}\n");
					exit();
				}
				$notified = $read_result->fetch(PDO::FETCH_ASSOC);
				
					echo("	NOTIFIED: ");	
					echo($notified['notified']); 
			
				return $notified;
				break;
				
			case "update":	
				$sql_update = "UPDATE sensor_readings 
								SET tstamp=tstamp, notified = true order by tstamp desc limit 1;";
				
				$update_result = $connection->query($sql_update);
				if ($update_result == FALSE){
					$info = $connection->errorInfo();
					echo("Error: {$info[2]}\n");
					exit();
				}
				return 1;
				

				/*
				echo "$result\n";
				
				if($result==$last_time_java){
					return 0; // App is up to date
				}else{
					return $result;  // App needs to be notified
				}*/
				break;
			}

}

function interact($socket) 
{ 	
		$notified_java=true;
		GLOBAL $last_time_java;
       $last_time_java = time();
        
        if (false === ($buf = socket_read($socket, 128, PHP_NORMAL_READ))) {
            echo "socket_read() failed: reason: " . socket_strerror(socket_last_error($socket)) . "\n";
            break 2;
        }
        
        if (strncmp($buf,"BASE",4)==0){
				//echo "BASE detected! ";
				// Comunicar com base de dados
				//AccessDatabase('insert',);
			//$last_time_base_sation;
			$packet_tstamp = new DateTime();
			list($protocol_msg, $tstamp, $packet_seqno) = sscanf($buf, "%s %d %d");
			$packet_tstamp->setTimestamp($tstamp);
			$i = AccessDatabase('insert', $packet_seqno, $packet_tstamp->format('Y-m-d H:i:s'), 1);
			echo "Sequence Nr: $packet_seqno	Time stamp: ";
			echo ($packet_tstamp->format('Y-m-d H:i:s')); // Se mandar o U como argumento vai o inteiro UNIX
			echo "	returned: $i\n";
			
		}
		if (strncmp($buf,"JAVA",4)==0){
			echo "JAVA: ";
			$notified = AccessDatabase('read');
			echo "Read from database; "; echo($notified['notified']); echo "\n";
			if(!$notified['notified']) {
				//echo "Notified: $notified; ";
				$str = sprintf("ALARM %d %d", time($notified['tstamp']), $notified['sequence_nr']); 
				socket_write($socket, $notified['tstamp'], strlen($notified['tstamp']));
				AccessDatabase('update');
				echo "Updated;\n";
			}else{
				socket_write($socket, "UPTODATE", strlen("UPTODATE"));
			}	
		}
} 

/** 
  * Become a daemon by forking and closing the parent 
  */ 
function become_daemon() 
{ 
    $pid = pcntl_fork(); 
    
    if ($pid == -1) 
    { 
        /* fork failed */ 
        echo "fork failure!\n"; 
        exit(); 
    }elseif ($pid) 
    { 
        /* close the parent */ 
        exit(); 
    }else 
    { 
        /* child becomes our daemon */ 
        posix_setsid(); 
        chdir('/'); 
        umask(0); 
        return posix_getpid(); 

    } 
} 

?>
