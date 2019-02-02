# Mnemosyne_server

Master Degree Thesis @ University of Padua

## Getting Started
1. Clone this repo (let's say in your home folder)

1. Install PostgreSQL using:
```bash
sudo apt-get install postgresql-client postgresql-client-common postgresql-contrib-9.6 postgresql-9.6 postgresql-client-9.6 postgresql-common postgresql-server-dev-9.6
```
If you cannot find this version, please add PostgreSQL apt repository using [this guide](https://wiki.postgresql.org/wiki/Apt).
Create then a user called 'mnemosyne' with the following commands:

```bash
sudo -u postgres psql
create user mnemosyne with encrypted password '<your-password>';
CREATE DATABASE mnemosyne WITH OWNER mnemosyne
GRANT CREATE ON DATABASE mnemosyne TO mnemosyne;
```
also, create the pg_crypto extension using:
```sql
\c mnemosyne
CREATE EXTENSION pgcrypto;
```
Finally, edit the `postgresql.conf` file in `/etc/postgresql/9.6/main/postgresql.conf` and set `listen_addresses` to `'*'`

3. Get the postgreSQL Java driver using `wget https://jdbc.postgresql.org/download/postgresql-9.4.1207.jar`

1. Move it to your java folder using `sudo mv postgresql-9.4.1207.jar $JAVA_HOME/jre/lib/ext`, if you have not set your JAVA_HOME 
variable, it's usually `/usr/lib/jvm/jdk-<version>-<details on your distribution>-<architecture>` otherwise a quick Google search will give you the answer (or any folder you have in `/usr/lib/jvm/` should do it).

1. Install phpPgAdmin using `sudo apt-get install phppgadmin`

1. Access phpPgAdmin typing `localhost/phppgadmin/` in any browser on the machine where you installed it.
If you're trying to access from anywhere else and you get a 403 Forbidden error, edit the `phppgadmin.conf` file in `/etc/apache2/conf-available/phppgadmin.conf` and comment the `Require local` line.
Reboot then Apache using `sudo service apache2 restart`

1. Login with user `mnemosyne` click on `schemas`, then on `Create new Schema` and create a schema named `mnemosyne`.

1. Copy-paste the contents of the sql file in `~/Mnemosyne_server/src/main/database/database.sql` and execute it removing the `Paginate results` check at the bottom. the same goes for the sql file in `~/Mnemosyne_server/src/main/database/populatedb.sql` which will add some accounts and some definitions.

1. Edit the file found in `~/Mnemosyne_server/src/main/webapp/META-INF/context.xml` and insert there the username of the owner of the `mnemosyne` schema and it's corresponding password below. Change also the `url="jdbc:postgresql:URL"` field replacing `URL` with the URL where postgreSQL can be reched (`url="jdbc:postgresql://localhost:5432/mnemosyne"` should be good)

1. Go to https://openrouteservice.org/, sign up and get an API key, then create the folder `~/Mnemosyne_server/src/main/resources/ap/mnemosyne/places/` and, inside it, create a new file named `openroute.key` and write there the API key you just got.

1. Install maven and compile using the following:
```bash
apt-get install maven
cd ~/Mnemosyne_server
mvn clean package resources:resources
```
It's gonna download tons of dependencies, go get a coffee

13. Install Tomcat 8 with `sudo apt-get install tomcat8 tomcat8-admin tomcat8-common`

1. Create a user for mnemosyne: edit the file `tomcat-users.xml` found in `/etc/tomcat8/` and add the following lines inside the `<tomcat-users>` tag:
```xml
<role rolename="manager-gui" />
<user username="mnemosyne" password="<your-password>" roles="manager-gui" />
```
Then in the manager `web.xml` file usually located in `/usr/share/tomcat8-admin/manager/WEB-INF` find and edit the following:
```xml
<multipart-config>
    <!– 50MB max –>
    <max-file-size>52428800</max-file-size>
    <max-request-size>52428800</max-request-size>
    <file-size-threshold>0</file-size-threshold>
</multipart-config>
```
Change both the `max-file-size` and `max-request-size` value to `314572800`

15. run `sudo service tomcat8 restart` to apply changes

16. type `localhost:8080` in any browser on the machine where tomcat is installed in to see if it's working and click on "manager webapp"

1. deploy the .war package that maven created in `~/Mnemosyne_server/target/mnemosyne.war`

You're finally good to go.

#### Extra steps
if you want to reset repeatable tasks/places in the `hasBeen` table every night, follow these steps:
1. install psycopg2 using this command : `pip install psycopg2`
1. Create a python script with the following code:
```python
#!/usr/bin/python
import psycopg2

def connect():
    """ Connect to the PostgreSQL database server """
    conn = None
    try:
        # connect to the PostgreSQL server
        print('Connecting to the PostgreSQL database...')
        conn = psycopg2.connect(host="localhost",database="mnemosyne", user="your-username", password="your-password")

        # create a cursor
        cur = conn.cursor()

        # execute a statement
        print('Executing statement...')
        stmt = ('UPDATE mnemosyne.task SET doneToday=false, failed=false WHERE repeatable=true;' +
                'UPDATE mnemosyne.task SET ignoredToday=false WHERE ignoredToday=true;' +
                'UPDATE mnemosyne.hasbeen SET beenthere=false;')
        cur.execute(stmt)
        conn.commit()

        # close the communication with the PostgreSQL
        cur.close()
        print('Done.')
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')


if __name__ == '__main__':
    connect()
```
3. type `crontab -e`
1. Add the following line to your crontab:
`0 3 * * * python /full/path/to/the/python/script`

Now this script will run every night at 3 A.M.

## OpenStreetMap Nominatim

In order to find places where to satisfy tasks, OpenStreetMap Nominatim service is used.
Documentation can be found here https://wiki.openstreetmap.org/wiki/Nominatim
