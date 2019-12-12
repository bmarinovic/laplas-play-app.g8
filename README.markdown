# laplas-play-app.g8
A [Giter8][g8] template with preset libraries and `sbt-laplas-codegen` plugin usually used by Laplacian organization!

You should only need to clone this project if you are modifying the giter8 template. For information on giter8 templates, please see http://www.foundweekends.org/giter8/

## Prerequisites
You should have sbt, scala and docker installed on your machine to follow this guide.

## Creating the project from template

If you want to create a project:

```bash
$ sbt new bmarinovic/laplas-play-app.g8
```

This will provide you with wizard where you can name your project and organization, along with library versions (ENTER means it takes default property value that is enclosed in brackets).
In this example, we will use `chronos` as project name leaving rest of the values default:
```
Creates new Scala Play project with script that generates code from Slick's Tables file. 

name [laplas-play-scala-seed]: chronos
organization [hr.laplacian]: 
useJwt [YES/no]: 
jodaVersion [2.10.1]: 
playJsonVersion [2.7.1]: 
playJodaVersion [2.7.1]: 
silencerVersion [1.3.1]: 
retryVersion [0.3.2]: 
laplasCommonsVersion [0.1.2]: 
chimneyVersion [0.3.0]: 
postgresqlVersion [42.2.5]: 
playSlickVersion [4.0.0]: 
playSlickEvolutionsVersion [4.0.0]: 
slickJodaMapperVersion [2.4.0]: 
jodaConvertVersion [2.2.0]: 
tsecVersion [0.0.1-M11]: 
playJsonExtensionVersion [0.40.2]: 
autoconfigMacrosVersion [0.2.0]: 
scalaFmtVersion [2.0.0]: 
slickCodegenPluginVersion [1.4.0]: 

Template applied in /home/user/git/chronos
```

## Compiling the project
Go to project's root and start sbt session:
```bash
$ cd /home/user/git/chronos
$ sbt
```
Once in sbt, you can compile the project:
```
compile
```
Open new bash session and start PostgreSQL container that will create `chronos` database:
```bash
$ cd /home/user/git/chronos/docker/
$ docker-compose up --build -d
Creating network "docker_default" with the default driver
Creating docker_postgres_1 ... done
```
This template comes with example Play evolution (`1.sql`) that contains `users` table with default Admin user and hashed password `admin123`.

Return to sbt session and run the Play application which will apply Play evolution when we go to `http://localhost:9000`:
```
run
```
We can now use `sbt-laplas-codegen` plugin that has `slickCodegen` task as a dependency:
```
laplasCodegen
```
This will create `Tables.scala` from `users` table as part of the `slickCodegen` task and also controller, api, domain and DAO layers as part of the `laplasCodegen` main task.

Open the `chronos` as a project in your favourite IDE and uncomment the code in `JwtController.scala`.
`JwtUserApiV1Impl.scala` also contains example code that you could use if you implement used method (`findByEmailAndSecret`).
This step is needed because there's no `Users` layers until we execute `laplasCodegen`, but we need the code to compile and run to generate the `Tables.scala` which is input to `laplasCodegen`.

Now we can compile and run the app again:
```
compile
run
```

## Testing the app

You can login to app using Postman or similar app:
```
POST http://localhost:9000/api/v1/jwt/login
```
```
{
	"username": "admin@laplacian.hr",
	"password": "admin123"
}
```

Create some user with `UserRole`:
```
http://localhost:9000/api/v1/users
```
```
{
	"email": "blaz@laplacian.hr",
	"firstName": "Blaz",
	"lastName": "Marinovic",
	"password": "bade123",
	"role": "UserRole"
}
```

You can go to browser and see all `Users` (paginated):
```
http://localhost:9000/api/v1/users?pageNumber=1&pageSize=10
```
Output:
```
{
  "pagination": {
    "pageNumber": 1,
    "pageSize": 10,
    "pageItemsOffset": 0
  },
  "totalRecords": 2,
  "totalPages": 1,
  "records": [
    {
      "id": 1,
      "email": "admin@laplacian.hr",
      "firstName": "Admin",
      "lastName": "Adminic",
      "password": "$2a$10$GqiG8fWtmajdNFJ4YhFvLu.86Oh3ITJb.0DWqsbPc5SWpptVQd/bG",
      "role": "Admin",
      "createdAt": "2019-12-11T23:04:19.663+01:00",
      "updatedAt": "2019-12-11T23:04:19.663+01:00"
    },
    {
      "id": 2,
      "email": "blaz@laplacian.hr",
      "firstName": "Blaz",
      "lastName": "Marinovic",
      "password": "bade123",
      "role": "UserRole",
      "createdAt": "2019-12-11T23:06:20.620+01:00",
      "updatedAt": "2019-12-11T23:06:20.644+01:00"
    }
  ]
}
```

**Note:**
> Until you implement `findByEmailAndSecret` functionality which is used in `JwtUserApiV1Impl.login(login: Contract.Login)`, every request will be treated as `AdminRole`.

#### Additional advice

Use `CryptoUtils.scala` to implement password hashing on DAO layer.

---

## Running template locally

If you are testing this giter8 template locally, you should [install g8](http://www.foundweekends.org/giter8/setup.html) and then run the [local test](http://www.foundweekends.org/giter8/testing.html) feature:

```bash
$ g8 file://laplas-play-app.g8/
```

### Template license
----------------
Written in 2019 by bmarinovic blaz@laplacian.hr
[other author/contributor lines as appropriate]

To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.

[g8]: http://www.foundweekends.org/giter8/
