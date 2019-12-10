#connect "http://localhost:8080/rdf4j-server".
show repositories.
create native.
labelingsystem
labelingsystemstore
10000
spoc

open labelingsystem.
load "/opt/defaultDevServer.ttl".
close.
quit.
