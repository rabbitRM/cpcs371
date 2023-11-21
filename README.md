### The Virtual Meeting Room Application

The Virtual Meeting Room application is based on the application titled
“A Multi-User Chat Application”. This application is provided in the following link.
A Multi-User Chat Application code link:
#### https://cs.lmu.edu/~ray/notes/javanetexamples/#chat
The basic idea of the Multi-User Chat Application is creating a chat server. The
server must broadcast recently incoming messages to all the clients
participating in a chat. This is done by having the server collect all the client
sockets in a dictionary, then sending new messages to each of them (as
described on the website).
The virtual meeting room applications adds on the above application’s
functionality the group checking function. Where the server checks whether the
new client is a member of the current meeting group or not.
