# FileUploader

The Client will upload MS Word documents (in bytes stream) to the Server using port number 5520. The Server only handles one Client at a time so there is no need for a java ServerThread class.

After the Server has received and saved the file, the Server sends back the single ASCII byte: '@' and
then disconnects from that client.

The Client disconnects when it gets back the '@'. If the client gets back a character other than '@', it still
disconnects but prints an error message.
