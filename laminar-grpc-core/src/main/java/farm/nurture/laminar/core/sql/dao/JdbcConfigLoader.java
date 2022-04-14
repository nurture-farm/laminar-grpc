/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package farm.nurture.laminar.core.sql.dao;

import farm.nurture.infra.util.ApplicationConfiguration;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JdbcConfigLoader extends DefaultHandler {

    private static final Logger logger = LoggerFactory.getLogger(JdbcConfigLoader.class);
    private ApplicationConfiguration conf = ApplicationConfiguration.getInstance();

    private List<DbConfig> configs = null;
    private DbConfig running = null;
    private StringBuilder sb = new StringBuilder();


    public static void main(String[] args) throws Exception {

        JdbcConfigLoader configLoader = new JdbcConfigLoader();
        String configXml =
            "<list>"
                + "	<pool>"
                + "		<poolName>configpool</poolName>"
                + "		<driverClass>com.mysql.jdbc.Driver</driverClass>"
                + "		<connectionUrl>jdbc:mysql://localhost/treedb?generateSimpleParameterMetadata=true</connectionUrl>"
                + "		<login>nurture</login>"
                + "		<password>nurture</password>"
                + "		<idleConnections>1</idleConnections>"
                + "		<maxConnections>1</maxConnections>"
                + "		<incrementBy>1</incrementBy>"
                + "		<testConnectionOnBorrow>false</testConnectionOnBorrow>"
                + "		<testConnectionOnIdle>true</testConnectionOnIdle>"
                + "		<testConnectionOnReturn>false</testConnectionOnReturn>"
                + "		<healthCheckDurationMillis>1800000</healthCheckDurationMillis>"
                + "		<timeBetweenConnections>5</timeBetweenConnections>"
                + "		<isolationLevel>1</isolationLevel>"
                + "		<allowMultiQueries>false</allowMultiQueries>"
                + "		<testSql>select 1</testSql>"
                + "		<runTestSql>true</runTestSql>"
                + "		<defaultPool>true</defaultPool>"
                + "	</pool>"
                + "</list>";

        logger.info("configXml = {}",configXml);
        JdbcConfigLoader loader = new JdbcConfigLoader();
        List<DbConfig> pools = loader.getConfiguration(configXml);
        logger.info("connection url = {}", pools.get(0).getConnectionUrl());
        PoolFactory.getInstance().setup(configXml);
    }

    public List<DbConfig> getConfiguration(final String xmlString) throws ParseException {
        if (null != configs) return configs;
        configs = new ArrayList<>();

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {

            saxParser = saxFactory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(xmlString)), this);

            return configs;

        } catch (Exception e) {
            logger.error("Schema: " + "\n" + xmlString, e);
            throw new ParseException(e.getMessage(), 0);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {

        sb.setLength(0);
        if (qName.equalsIgnoreCase("pool")) {
            try {
                running = new DbConfig();
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("pool")) {
            configs.add(this.running);
            running = null;
        } else if (qName.equalsIgnoreCase("poolName")) {
            running.setPoolName(sb.toString());
        } else if (qName.equalsIgnoreCase("driverClass")) {
            running.setDriverClass(sb.toString());
        } else if (qName.equalsIgnoreCase("connectionUrl")) {
            String connectionUrl = conf.get("database_connectionUrl");
            logger.info("database_connectionUrl:{}",connectionUrl);
            if (null == connectionUrl) running.setConnectionUrl(sb.toString());
            else running.setConnectionUrl(connectionUrl);
        } else if (qName.equalsIgnoreCase("login")) {
            String login = conf.get("database_login");
            logger.info("database_login: {}",login);
            if (null == login) running.setLogin(sb.toString());
            else running.setLogin(login);
        } else if (qName.equalsIgnoreCase("password")) {
            String password = conf.get("database_password");
            if (null == password) running.setPassword(sb.toString());
            else running.setPassword(password);
        } else if (qName.equalsIgnoreCase("idleConnections")) {
            String idleConnections = conf.get("database.idleConnections");
            if (null == idleConnections) running.setIdleConnections(Integer.parseInt(sb.toString()));
            else running.setIdleConnections(Integer.parseInt(idleConnections));
        } else if (qName.equalsIgnoreCase("maxConnections")) {
            String maxConnections = conf.get("database.maxConnections");
            if (null == maxConnections) running.setMaxConnections(Integer.parseInt(sb.toString()));
            else running.setMaxConnections(Integer.parseInt(maxConnections));
        } else if (qName.equalsIgnoreCase("incrementBy")) {
            running.setIncrementBy(Integer.parseInt(sb.toString()));
        } else if (qName.equalsIgnoreCase("testConnectionOnBorrow")) {
            running.setTestConnectionOnBorrow(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("testConnectionOnIdle")) {
            running.setTestConnectionOnIdle(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("testConnectionOnReturn")) {
            running.setTestConnectionOnReturn(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("connectionTimeoutMillis")) {
            running.setConnectionTimeoutMillis(Long.parseLong(sb.toString()));
        } else if (qName.equalsIgnoreCase("validationTimeoutMillis")) {
            running.setValidationTimeoutMillis(Long.parseLong(sb.toString()));
        } else if (qName.equalsIgnoreCase("healthCheckDurationMillis")) {
            running.setHealthCheckDurationMillis(Integer.parseInt(sb.toString()));
        } else if (qName.equalsIgnoreCase("timeBetweenConnections")) {
            running.setTimeBetweenConnections(Integer.parseInt(sb.toString()));
        } else if (qName.equalsIgnoreCase("isolationLevel")) {
            running.setIsolationLevel(Integer.parseInt(sb.toString()));
        } else if (qName.equalsIgnoreCase("allowMultiQueries")) {
            running.setAllowMultiQueries(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("testSql")) {
            running.setTestSql(sb.toString());
        } else if (qName.equalsIgnoreCase("runTestSql")) {
            running.setRunTestSql(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("defaultPool")) {
            running.setDefaultPool(Boolean.parseBoolean(sb.toString()));
        } else if (qName.equalsIgnoreCase("preparedStmt")) {
            running.setPreparedStmt(Boolean.parseBoolean(sb.toString()));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        sb.append(new String(ch, start, length));
    }
}
