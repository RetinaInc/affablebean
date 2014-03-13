AffableBean (osmanpub revision)
===============================

Introduction
------------

This update builds upon the excellent tutorial in the Netbeans documentation:

https://netbeans.org/kb/docs/javaee/ecommerce/intro.html

It's worth reading and following the whole tutorial and building the sample code
so that you'll have a good understanding of this project.


Requirements
------------

You'll need Jave EE 7 with Glassfish 4.0 installed along with MySQL 5 or later.
The project uses Maven with modules for the back, middle and front tiers. 

Checkout the code from https://github.com/osmanpub/affablebean.git and navigate to 
either the oracle or google folder from your IDE to load the master POM file.
Do an initial build to see if your installation and configuration are working.

Next run the code, if you have problems then chances are they could be database
connectivity or security issues from Glassfish. The tutorial explains how to 
solve these problems so refer to it if necessary.

The important modules are ejb, web and html5 with the last module contaning 
the angular front-end (more on that later).


Ejb module
----------

Changes:

* Addition of EJB's for Promotion and Message Feedback entities
* Replaced sychronization with concurrent data structures 
* Database setup code moved to resources/setup. Additional promotion and sale
  scripts are included if you want to apply them later. The original setup file
  is also included.
* The cart and session packages are moved here from the web module


Web module
----------

Changes:

* Presentation improved with addition of Bootstrap
* Additional product categories: cereals and drinks
* Addition of Spanish language locale
* Several promotional campaigns added: general sale, category and product offers
* Privacy and contact pages added
* Feedback from contact page viewable on admin site
* Configuration moved from Java to XML to make it more consistent 
* JSON support added for HTML5. You can view Json data sent by enabling
  the "showJson" option from 0 to 1 in the web.xml. A button will be displayed
  in the footer allowing you to see what is sent to the client.


Html5 module (new)
------------------

The screens are nearly identical to the web module with the use of angular and 
bootstrap frameworks. The functionality remains the same. Data exchange is done
with the web module in JSON format.

Packaging it as a Jave web application that can be hosted on the same server as 
the web module circumvents Cross-origin resource sharing (CORS) issues as well 
as enabling debugging facitilites to be used in Netbeans.

However there are still some issues:

* You may need to reload the index page to get it to display categories properly
* Shopping cart will remain after confirmation of purchase if you use the back 
  button instead of navigating to index page

Changes:

* Addition of search and sort to product and cart screens
* No admin site equivalent of web module
* Checkout page not secured


Google Hosting
--------------

JSP Web module: https://sylvan-cycle-510.appspot.com/

HTML5 module: https://sylvan-cycle-510.appspot.com/index2.html

(press refresh if the images are loading incorrectly)


Outro
-----

There is a Java EE 6 Html5 version that does something similar here:

https://bitbucket.org/dkonecny/affable-bean/wiki/Home

Use osmanpub@gmail.com for bug reports and feedback.
