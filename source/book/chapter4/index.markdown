---
layout: default 
title: "Practical Threat Modeling"
---
NOTE: This chapter is currently in progress. It's being posted for peer
review during the writing process.


**Threat modeling** is the art of thinking about what threats your
application will face. By understanding the threats, you can begin thinking
about how to mitigate them.

One useful tool when threat modeling is an **attack tree**. In an attack tree,
you identify what threats you might face, and what those attacks lead to. During
the initial stages, you don't focus on mitigation (or what you have done so far).
It's important to consider what sorts of attacks can be made: while you might
think an attack is mitigated or you may not see it as practical, changes to the
code can easily change the situation. It's important to regularly review the
attack tree, updating it as necessary. If you add a feature, consider the 
potential security impact. The point of the attack tree is just to visualise 
what could happen; later parts of the security engineering process transform
this into a practical outlook.

Let's consider a simple web site that allows users to update their information;
it's backed by a fairly simple database. Some potential attack vectors that 
come to mind in a quick overview are:

0. SQL injection on the login form -> privilege escalation
0. password interception on the wire -> stealing user's account information
0. password brute forcing -> stealing user's account information
0. unauthorised access to resources, i.e. user B can read or update user A's
information without their consent.

It's also helpful to identify vulnerability points with these:

0. login form (SQL injection, password brute forcing)
0. the wire, i.e. the connection between the user and the server (password
interception)
0. whereever user information can be looked up

From here, we can identify mitigation steps fairly easily:

0. Sanitize all user inputs.
0. Secure the connection using SSL.
0. Use [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) to store passwords.
0. Force all sensitive information to be routed through authorisation 
middleware.

Generally, it's good practise to keep these in separate documents; i.e. an
attack tree (though often annotated with the vulnerability points), and
a security plan. This allows you to consider the two separately when needed.
The security plan should reference the attack tree in considering what 
attacks are being defended against. These are also living documents; as
mentioned before, they will need to be reviewed and updated regularly.

## Identifying Attack Vectors
A good place to check for attack vectors is to consider the generic attack
points:

0. Where does data enter the system? Unsanitised data can pose a risk, as can
data that is not normalised to expected values.
0. Where does data leave the system? **Information leakage** is a security
issue where information that should not leave the system does, or is revealed
by the system unintentionally (typically without effort on the part of the
attacker).
0. What sensitive data does the system use? Passwords an obvious answer that
come to find first; also, consider email addresses and other personal details.
Resources that should not be widely accessible to the public fall under this
category as well; for example, if you are publishing a paid magazine online,
you want to consider restricting access to it.
0. Where and how is sensitive information stored? For example, when passwords
aren't being used, how are they being stored? Are you using bcrypt or are they
being stored in plaintext?

This can be a difficult task. You have to be creative, and you will likely still 
not consider everything an attacker will. Having several people involved in 
developing the attack tree is very useful, as they will all have different
mindsets and backgrounds to contribute. One technique I find useful is to
visualise your program as a system, diagramming subsystems and information flow.
Just as physical security engineers will study the physical layout of the area
they are working to protect and bank robbers will often observe their target
beforehand, it is another tool useful for visualising where security systems
fit into the overall architecture.


## Example
Let's consider an example: I have a website that provides a front end for my
small team's ebook library. Here is the systems diagram I have for the site:

![systems diagram](images/dystopia_system_diagram.png)

And here is a sample attack tree I've come up with:

![attack tree](images/dystopia_attack_tree.png)

What are the attack vectors? If we organise them by the four generic attack 
points, we could come up with:

0. Where does data enter the system?
    * login form
    * user book uploads
    * user metadata fields
    * book cover images
    * user bio
0. Where does data leave the system?
    * book download
    * user bio display
    * displaying catalog information
    * book cover display
0. What sensitive data is present in the system?
    * passwords
    * user catalog
0. Where and how is sensitive information stored?
    * passwords: stored in the database as bcrypted hashes
    * user catalog: stored in the database and accessed through an access control list (ACL)



