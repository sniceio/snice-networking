# Snice Networking

TODO:

* Need to invoke Peer FSM for Answers, which need to be matched against outstanding transactions and remembered in case we get a re-transmission.
* Need to purge the outstanding transactions at some point. Now they just keep filling up.
* Need to work out a better Connection strategy at the NettyApplicationLayer. Should be a caching one but need to be able to fill out the various parts and currently I don't have the actual original connection context.
* Probably should create a MessageIOEvent and send that one down but at the same time, the only reason I would need to do so would be for keeping track of the start time of the message. So, not sure I really need it.
* When invoking the Peer FSM, we really need to know the direction of the message. Perhaps we do need to create a corresponding I and R-xxxx events like the spec. Or, perhaps it is enough for MessageIOEvent that has a "isUpstream"/"isDownstream". Then the FSMs can work off of that. We would need the same in the SIP Transaction case (TODO: check what I did there)

Potentially
* Allow for DiameterBuilder to be sent down.
