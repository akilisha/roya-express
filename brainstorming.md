So let's say you have the following paths to map for your application:

GET     /
GET     /hello
POST    /api/user
GET     /api/user/:uid
PUT     /api/user/:uid
DELETE  /api/user/:uid
POST    /api/user/:uid/address
GET     /api/user/:uid/address/:aid
PUT     /api/user/:uid/address/:aid
DELETE  /api/user/:uid/address/:aid
GET     /api/user/address/state/\/^i.*a$\/
GET     /\/[0-9]+-[[\\w]]*\/
GET     /\/[0-9]+-[[\\S]]*\/
GET     /\/[[\\s\\S]]*\/
GET     /\/ab?cd\/                              match acd and abcd
GET     /\/ab+cd\/                              match abcd, abbcd, abbbcd
GET     /\/ab.*cd\/                              match abcd, abxcd, abRANDOMcd, ab123cd
GET     /\/ab(cd)?e\/                           match /abe and /abcde
GET     /\/.*fly$\/                             natch butterfly and dragonfly, but not butterflyman, dragonflyman

There's a certain consistency that can be exploited for handling routing, like searching a tree, and each level splits the search space by the number of branch nodes

Consistent characteristics about a path:
1. HTTP method - GET, POST, PUT, DELETE, PATCH
2. Path segments (not counting slash) - for example
   - / => [""]
   - /hello => ["hello"]
   - /api/user/:uid => ["api", "user", "(.*)"] -> each variable param is converted into a regex matcher literal for example ":uid" will become "(.*)" meaning it will match anything to the end of the path 
   - /api/user/:uid/address/:aid => ["api", "user", "(.*?)/", "address", "(.*)"] -> in this case, ":uid" is replaced by "(.*?)/" which will matching anything until the nearest "/" in the path, excluding the "/", while ":aid" will match anything to the end of the path
   - /\/[0-9]+-[[\\w]]*\/ => ["([0-9]+-[[\\w]]*)?/"] -> in this case that entire segment is a regex, so it will be preserved in the segment array
   - /\/ab?cd\/ => ["(ab?cd)"] -> similarly, regex is preserved in the segments array
3. Now the search tree can be constructed like this
   root router
    |-- POST router
    |    |----"<node>"
    |    |----"api"
    |           |----"user"
    |                  |----<node>
    |                  |----"(.*?)/"
    |                         |----"address"
    |                                |----<node>
    |-- GET router
    |    |----"hello"
    |           |----<node>
    |    |----"([0-9]+-[[\\w]]*)?/"
    |           |----<node>
    |    |----"(ab?cd)"
    |           |----<node>
    |    |----"api"
    |           |----"user"
    |                  |----<node>
    |                  |----"(.*?)/"
    |                         |----"address"
    |                                |----"(.*)"
    |                                |      |----<node>
    |                                |----"state"
    |                                       |----"(^i.*a$)"
    |-- PUT router
    |    |----"api"
    |           |----"user"
    |                  |----<node>
    |                  |----"(.*?)/"
    |                         |----"address"
    |                                |----"(.*)"
    |                                       |----<node>
    |-- DELETE router
    |    |----"api"
    |           |----"user"
    |                  |----<node>
    |                  |----"(.*?)/"
    |                         |----"address"
    |                                |----"(.*)"
    |                                       |----<node>
    |-- PATCH router
    |    |----"api"
    |           |----"user"
    |                  |----<node>
    |                  |----"(.*?)/"
    |                         |----"address"
    |                                |----"(.*)"
    |                                       |----<node>
4. Based on this arrangement, when a request comes in, it will be split into segments, and each segment will be matched against the tree's nodes (routers), depth-first-search. The "<node>" notation
denotes a router in the three. In doing so, we are untethered from the routing of Helidon, which is quite basic
5. For each path param, the values is extracted for the incoming request path, and added to a params map
6. In the request, a user can now do the following with params:
   - ".params()" => return all params as a map
   - ".param(String)" => return named param as object
   - ".param(Class)" => return named param converted into specified Class type. This implies that there should be a type conversion factory for path params with default done by the framework, but the user has options to define concrete converters, and getting them registered perhaps through the help of a plugin

Are you able to follow my router search algorithm? How does this compare to how you are currently implementing path search? 


Nested routing
Earlier we were able to figure out how to handle regex parts in the request path - by using path segments and segment matchers. While this was a great milestone, 
there was one thing that kept gnawing at my mind. And this is about the ".any(...)" method. I have been wondering how on earth that method would be useful. 
Which use-case would possibly need this functionality? I then it hit me. Aha! There is one such use-case after all.

This use-case seems ideal for handling nested routers, which I've been racking my brain about wondering how to implement. So this is the algorithm, and tell me what you think.

root router
|-- [POST|GET|PUT|PATCH|DELETE] routers
|    |----"tracing"
|           |----"<node>"
|    |----"metrics"
|           |----"users"
|                  |----"<node>"
|           |----"products"
|                  |----"<node>"
|-- ALL router
|    |----"api"
|           |----<Nested Router>
|                  |----GET router
|                        |----"users"
|                                |----"(.*)"
|                                       |----<node>
|                  |----POST
|                        |----"users"
|     |----"health"
|           |----<Nested Router>
|                  |----GET router
|                        |----"<node>"


So how would this work? 

GET /api/user/:id

Request handler resolution. It will always search DFS on if a matching method handler exists (at that level). 
Otherwise, it will speak breadth-wise (at that level) to the ALL matcher, to find a nested router in that path

root router 
  -> GET
    -> search for /api/user/:id fails
      -> bubbles bck
  -> ALL
    -> it will find /api
      -> nested router
        -> it will find /users/(:id)

