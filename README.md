Peking University Open Research Data Platform
==========

PKU Open Research Data is a customization of Harvard Dataverse, including:
(1) bilingual interface (Chinese and English): not only the elements in Bundle.properties file, but also the metadata of dataverse and dataset.
(2) authorization: implement GroupProvider and CredentialsAuthenticationProvider for Peking University users.
(3) usage statistic: logging the events of viewing dataset, viewing dataverse, downloading file, joining group request, accept join group request and reject join group request. Indexing the events and providing for manager of dataverse or dataset to search and view them.
(4) datacite doi (https://mds.datacite.org/) register module.
(5) home page to display featured dataverses no matter where it is in the dataverse tree.
(6) user management based on explicit group: display explicit group on dataset page so that user can apply for joining group, a explicit group management interface so that dataverse manager can accept, reject, delete, add user.
(7) and others ...

Wellcome to visit our service http://opendata.pku.edu.cn/ .


Dataverse 
==========

Dataverse is an open source web application for sharing, citing, analyzing, and preserving research data (developed by the [Data Science team] (http://datascience.iq.harvard.edu/about-dataverse) at the [Institute for Quantitative Social Science] (http://iq.harvard.edu/)).

Institutions and organizations can choose to install the Dataverse software for their own use. 
In this case, the institution will be responsible for maintaining the application; installing upgrades, 
setting up backups and data replication as needed. Dataverse 4.0 is now released and used by the Harvard Dataverse: [dataverse.harvard.edu] (http://dataverse.harvard.edu/). If you'd like to first test it, you can use our demo site: [dataverse-demo.iq.harvard.edu] (http://dataverse-demo.iq.harvard.edu/).

For more general information about Dataverse please visit
[dataverse.org] (http://dataverse.org). 

The Dataverse code is *open-source* and *free*. 

Installation packages are in Sourceforge for installers. [Download the latest production stable 
version of the Dataverse application] (http://sourceforge.net/projects/dvn/files/dvn/).

[![Dataverse Project logo](src/main/webapp/resources/images/dataverseproject_logo.jpg?raw=true "Dataverse Project")](http://dataverse.org)
