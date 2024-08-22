# qqq

This is the top-level/parent project of qqq.

QQQ is a Low-code Application Framework for Engineers.

## Artifacts
*Note, this information - well, I'd say it's out of date, but honestly, I don't
think this was ever accurate, lol.  Either way, it needs re-written, please.
Should refrence the bom-pom, and there is no "bundle" concept at present.*

> QQQ can be used with a single bundle or smaller fine grained jars.
> The bundle contains all of the sub-jars.  It is named:
> 
> ```qqq-${version}.jar```
> 
> You can also use fine-grained jars:
> - `qqq-backend-core`: The core module.  Useful if you're developing other modules.
> - `qqq-backend-module-rdbms`: Backend module for working with Relational Databases.
> - `qqq-backend-module-filesystem`: Backend module for working with Filesystems (including AWS S3).
> - `qqq-middleware-javalin`: Middleware http server.  Procivdes REST API, and/or backing for a web frotnend.
> - `qqq-middleware-picocli`: Middleware (actually, a front-end, innint?) Command Line interface.

## Framework Developer Tools/Resources
### IntelliJ
There are a few useful IntelliJ settings files, under `qqq-dev-tools/intellij`:
- Kingsrook_Code_Style.xml
- Kingsrook_Copyright_Profile.xml

One will likely also want the [Kingsrook Commentator
Plugin](https://plugins.jetbrains.com/plugin/19325-kingsrook-commentator).

## License
QQQ - Low-code Application Framework for Engineers. \
Copyright (C) 2020-2024.  Kingsrook, LLC \
651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States \
contact@kingsrook.com | https://github.com/Kingsrook/

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

