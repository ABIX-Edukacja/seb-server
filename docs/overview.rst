Overview
========

Safe Exam Browser Server (SEB Server) is a web-service application to help setting up and maintain e-assessments with 
`Safe Exam Browser <https://safeexambrowser.org/>`_ (SEB) in a centralized manner. The SEB Server application can be used 
by an institution or organization as a self-maintained, lightweight server application to organize and setup their e-assessments. 
Furthermore SEB Server can be setup and run within a cloud infrastructure by general service provider.

For an organization or institution that wants to provides safe e-assessments with `Safe Exam Browser <https://safeexambrowser.org/>`_, 
SEB Server can help to organize this e-assessments more effectively, safely and in one place.

SEB Server comes with a built-in multi-tenancy functionality that allows to create and maintain institutions that are separated 
by each other. This is most convenient for smaller groups of organizations or institutions that want to have one self maintained 
SEB Server instance but need internal separation of institutions.

Since SEB Server is generally an administration tool, meaning used to do administration work and task to setup and maintain e-assessments, 
there is yet just a built-in small user-account management where users can register itself and get needed privileges assigned by already registered 
administrator users that has the privileges to maintain user accounts. Currently there is no possibility to register with a third 
party account and single sign on.

SEB Server is written in Java and uses Docker for installation and setup. For more information about the architecture and installation, 
please go to `SEB Server Installation Guide <https://seb-server-setup.readthedocs.io/en/latest/overview.html>`_

.. _roles_and_usecases:

Roles and Use-Cases
-----------------------

The SEB Server supports a simple role based privilege system where a role has read, modify and write privileges on certain domain entities. 
The privileges for a role are defined and cannot be changed by a user. Roles can be combined within one user-account.

Privileges for domain objects are categorized in read, modify and write where write includes creation and deletion grants in addition to the more 
restrict modify right that only allows to modify already created entities. They are also categorized in overall, institutional and owner privileges where 
overall means for all entities, over all available institutions and institutional means only the entities that belongs to the own institution 
and owner means the creator or assigned owner of an entity.

There are four different roles. This reflect a good separation of concerns within the SEB Server application administration itself and the setup and 
maintaining work that has to be done for e-assessments with SEB.

below are the names and a short description of each role.


**SEB Server Administrator**

This role is primarily to administer the SEB Server application, create new and maintain available institutions and user-accounts. In addition to that a 
SEB Server administrator also has overall read privileges for the most parts to be able to analyze problems and help others to solve them.

A typical use-case for a SEB Server administrator is to create a new institution and an institutional administrator for this institution.
Another use-case would be to maintain registered user accounts, to give more privileges or the entirely deactivate a exisitng user account.

**Institutional Administrator**

An institutional administrator has overall read access for its institution and is able to modify the institution properties as well as 
create new or maintain existing user-accounts for the belonging institution. Furthermore an institutional administrator is able to create and maintain 
learning management system setups and SEB client-configurations for the belonging institution.

A typical use-case for a institutional administrator is to give the appropriate roles and privileges to newly registered users of the institution.
Or another use-case would be to create and maintain SEB client configurations for the institutions and setup and maintain learning management systems 
within the SEB Server and the belonging institution.

**Exam Administrator**

With this role a user is able to prepare an exam for an a-assessment with SEB. A exam administrator is able to view all courses form all
learning management systems that are setup for the belonging institution and find a particular course to import this course as an exam
within SEB Server. This role is also able to create and maintain SEB exam configurations that can then be assigned to an imported exam.
After setting importing an exam, the role is able to setup all aspects of an exam like automated restrictions, allowed supporter and 
monitoring indicator, to name a few, and prepare the exam for an e-assessment.

A typical use-case for an exam administrator is to create a SEB exam configuration within the templating and configuration section. 
Then finding a specific course or quiz from the learning management systems that are defined within the institution. This course or quiz 
can then be imported by the administrator as an exam. After that an exam administrator will prepare this exam for run and monitoring by attaching 
a exam configuration to it, prepare monitoring indicators and specify the SEB restriction conditions.

**Exam Supporter**

This role is to support a running exam within SEB Server. An exam administrator is able to see the running exams on that he/she has 
an assignment and open them either for monitoring or editing some exam attributes or configuration during the exam.

A typical use-case for an exam supporter is on the time an exam is running, to overview the connecting SEB clients and manage them. 
While SEB server shows incidences or irregularities, a exam supporter can act on them and take the appropriate actions if needed.

Sign Up / Sign In
-----------------

What one probably see first when applying to a SEB server application is the login-screen

.. image:: images/overview/login.png
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/login.png

If you are already registered you can use your username and password to log in. Or use the "Register" button to go the the register 
page to create a new user-account. The newly created user account will only have the Exam Supporter role assigned. If one need another 
role or more privileges, this must be given and granted by an institutional administrator of the specified institution.

.. image:: images/overview/register.png
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/register.png

Fill in the register form and create a new account. After successfully creation, the application redirects to the login page for login. 
On the current version, e-mail confirmation is not a feature of the SEB Server application and therefore the e-mail address is just informative by now.

With the "Time Zone" one can choose a specific time zone for an user-account. All dates and times will then be showed within this specific time zone to the user.

.. _gui-label:

Graphical User Interface
------------------------

After successful login, one will see the main graphical user interface of the SEB Server application. On the left hand are the activities 
that can be done, categorized within some few main sections with its relating activities underneath. By selecting a activity, 
the main content section will show the activity content and the action that are possible for this activity on the right hand

.. image:: images/overview/overview.png
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/overview.png

In the header above on the right hand, we see the username of the currently logged in user and an action button the sign out and go back to the login page.

The main content usually consist of a list or a form.

**Lists**

A list shows all the objects of a particular activity in a table page. A list has paging and if a list has more objects than it fit on one page, 
a page navigation is shown at the bottom of the list with the information of the current page and the number of pages along with a page navigation 
that can be used to navigate forward and backward thought the list pages.

Almost all lists have the ability to filter the content by certain column filter that are right above the corresponding columns. 
To filter a list one can use the column filter input to narrow down a specific collection of content. Accordingly to the value type of the column, 
there are different types of filter:

- Selection, to select one instance of a defined collection of values (drop-down).
- Text input, to write some text that a value must contain.
- Date selection, To select a from-date from a date-picker. A date selection can also have an additional time selection within separate input field
- Date range selection, To select a from- and a to-date within different inputs and a date-picker. A date range selection can also have an additional time range selection within separate input fields

.. image:: images/overview/list.png
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/list.png

A list can be sorted within a column by clicking in the column header. The order of sorting can be changed by clicking again on the same 
column header of the sorted column. If sorting functionality is available for a column depends on the column type. There are a few columns 
that do not have a sort functionality yet.
Most columns have a short tool-tip description that pops up while the mouse pointer stays over the column header for a moment. 
A column tool-tip usually also explains how to use the column-related filter

**Forms**

Forms are used for object specific data input or presentation, like HTML Forms usually do. Forms appear in three 
different ways within the SEB Server GUI:

- When a object is first created in edit mode
- When an object is modified also in edit mode
- And when an object is just shown, in read-only mode

The following images shows the same form, once in read-only mode and once edit mode

.. image:: images/overview/form_readonly.png
    :alt: Form in read-only mode
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/form_readonly.png

.. image:: images/overview/form_edit.png
    :alt: Form in edit mode
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/overview/form_edit.png

There usually there is a tool-tip on a form field element that is activated either by going over and stay on the form field label or the input section. 
If a form field is mandatory to either create or save an object, this is marked within a little red arrow just to the right of the form field label. 
There may be more validation take place on saving the object. If a input needs a special form that is not given by the current input, the form-field 
will be marked with a red border and a thin red explanation text is shown right below the input field. After correct the missing or wrong input and save again, 
the SEB Server will accept and process the changes. If the user navigates away from a form in edit mode, the GUI will inform about possible data loss and asks for proceed or abort.