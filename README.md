sdfs
====

Secure Distributed File System

Members:
Nataraj Kaushik
Sameer Vijaykar


To build:

1. Make sure you have JDK version 1.6 or higher.
2. Run the following command from the directory containing this file.
		ant


To run:

1. To run the server, enter the following command (the argument to the command is the password to the server's keystore)
		./server server

2. To run the client, enter the following command (the arguments to the command are the alias for the client [node_a, node_b, node_c], the port on which to bind the client on (make sure to use different ports if running multiple clients on localhost), and the password for the client's keystore [nodea, nodeb, nodec])
		./client node_a 11111 nodea


Test cases:

The client reads and write files from the "data/files/<client_alias>" directory in the current folder. The files "file1.txt" and "file2.txt" have already been created here for testing purposes. The following commands can be run to test various use cases:

$server> ./server server

$client1> ./client node_a 11111 nodea
$client1> start localhost
$client1> put file1.txt
$client1> get file1.txt

$client2> ./client node_b 22222 nodeb
$client2> start localhost
$client2> put file2.txt
$client2> get file2.txt

$client3> ./client node_c 33333 nodec

$client1> get file2.txt			## will fail
$client2> get file1.txt			## will fail

$client1> delegate file1.txt node_b localhost:22222 120 read write					# A gives B read and write access to file1.txt for 2 minutes
$client2> get file1.txt			## succeeds
$client2> put file2.txt			## succeeds
$client2> delegate file1.txt node_c localhost:33333 300 read		##will fail		# B tries to delegate file1.txt to C, but fails

$client2> delegate file2.txt node_a localhost:11111 120 read delegate				# B gives A read and delegate access to file2.txt for 2 minutes
$client1> get file2.txt			## succeeds
$client1> put file2.txt			## will fail
$client1> delegate file2.txt node_c localhost:33333 300 read write					# A gives C read and write access to file2.txt for 2 minutes
$client3> get file2.txt			## succeeds
$client3> put file2.txt			## will fail as A didn't have permission to delegate "write" rights

## After 120 seconds
$client3> get file2.txt			## will fail as B had only given access to A for 120 seconds