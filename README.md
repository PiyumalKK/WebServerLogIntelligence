# 🌐 Web Server Log Intelligence

### Large-Scale Web Server Log Analysis Using MapReduce on Azure HDInsight

> **Module**: EE7222/EC7204 - Cloud Computing | University of Ruhuna  
> **Dataset**: [Web Server Access Logs](https://www.kaggle.com/datasets/eliasdabbas/web-server-access-logs) (Kaggle)  
> **Technology**: Java, Apache Hadoop MapReduce, Azure HDInsight

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Dataset Description](#dataset-description)
3. [Analysis Tasks](#analysis-tasks)
4. [Project Structure](#project-structure)
5. [Prerequisites](#prerequisites)
6. [Setup Instructions](#setup-instructions)
   - [Local Development Setup](#1-local-development-setup)
   - [Azure HDInsight Setup](#2-azure-hdinsight-cluster-setup)
7. [Building the Project](#building-the-project)
8. [Running the MapReduce Jobs](#running-the-mapreduce-jobs)
   - [Local Testing](#option-a-local--standalone-testing-no-hadoop-required)
   - [Azure HDInsight](#option-b-running-on-azure-hdinsight)
9. [Expected Output](#expected-output)
10. [Results Interpretation](#results-interpretation)
11. [Clean Up Azure Resources](#clean-up-azure-resources)

---

## Project Overview

This project implements a **Java-based MapReduce solution** that processes 3.3GB of Apache web server access logs to extract meaningful intelligence about web traffic patterns. The solution runs on **Azure HDInsight** (a managed Hadoop cluster in the cloud) and performs **5 distinct analyses** on the log data.

The project demonstrates the power of distributed computing for processing large-scale log files that would be challenging to analyze on a single machine.

---

## Dataset Description

| Property | Details |
|----------|---------|
| **Name** | Web Server Access Logs |
| **Source** | [Kaggle - eliasdabbas](https://www.kaggle.com/datasets/eliasdabbas/web-server-access-logs) |
| **Size** | ~3.3 GB (compressed ~280 MB) |
| **Format** | Apache Combined Log Format |
| **Origin** | Iranian ecommerce website (zanbil.ir) |
| **Records** | ~10 million+ log entries |
| **License** | CC0: Public Domain |

### Log Format (Apache Combined Log Format)

```
IP - - [timestamp] "METHOD URL PROTOCOL" statusCode size "referrer" "userAgent"
```

**Example line:**
```
54.36.149.41 - - [22/Jan/2019:03:56:14 +0330] "GET /filter/27|13 HTTP/1.1" 200 30577 "-" "Mozilla/5.0 (compatible; AhrefsBot/6.1)"
```

---

## Analysis Tasks

| # | Analysis | Description | Mapper | Reducer |
|---|----------|-------------|--------|---------|
| 1 | **HTTP Status Code Distribution** | Counts occurrences of each status code (200, 301, 404, 500, etc.) | `StatusCodeMapper` | `SumReducer` |
| 2 | **Top 10 IP Addresses** | Identifies the most active visitors/bots by request count | `IPCountMapper` | `TopNReducer` |
| 3 | **Hourly Traffic Pattern** | Reveals peak and off-peak hours (00:00–23:59) | `HourlyTrafficMapper` | `SumReducer` |
| 4 | **Top 10 Most Accessed URLs** | Finds the most popular pages/endpoints | `TopURLMapper` | `TopNReducer` |
| 5 | **HTTP Method Distribution** | Shows GET vs POST vs PUT vs DELETE breakdown | `HTTPMethodMapper` | `SumReducer` |

---

## Project Structure

```
WebServerLogIntelligence/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── .gitignore                       # Git ignore rules
├── report/
│   └── report_template.md           # 2-page report template
├── sample-data/
│   └── sample_input.log             # Sample data for local testing
└── src/main/java/com/weblog/
    ├── driver/
    │   └── LogAnalysisDriver.java   # Main driver (entry point)
    ├── mapper/
    │   ├── StatusCodeMapper.java    # HTTP status code mapper
    │   ├── IPCountMapper.java       # IP address count mapper
    │   ├── HourlyTrafficMapper.java # Hourly traffic mapper
    │   ├── TopURLMapper.java        # URL frequency mapper
    │   └── HTTPMethodMapper.java    # HTTP method mapper
    ├── reducer/
    │   ├── SumReducer.java          # Generic sum reducer (reusable)
    │   └── TopNReducer.java         # Top-N aggregation reducer
    └── util/
        └── LogParser.java           # Apache log format parser
```

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java JDK** | 1.8+ | Compile and run Java code |
| **Apache Maven** | 3.6+ | Build management and dependency resolution |
| **Azure Subscription** | Active | Create HDInsight cluster via Azure Portal |

### Install Prerequisites

```bash
# Verify Java
java -version
javac -version

# Verify Maven
mvn -version
```

> **Note**: All Azure resource provisioning is done through the [Azure Portal](https://portal.azure.com).

---

## Setup Instructions

### 1. Local Development Setup

```bash
# Clone/download the project
cd WebServerLogIntelligence

# Download the dataset from Kaggle
# Go to: https://www.kaggle.com/datasets/eliasdabbas/web-server-access-logs
# Click "Download" and extract the access.log file
```

### 2. Azure HDInsight Cluster Setup

> **Note**: All Azure resources are created via the [Azure Portal](https://portal.azure.com). 

#### Step 1: Login to Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Sign in with your Azure account

#### Step 2: Create a Resource Group

1. In the Azure Portal, click **"Create a resource"**
2. Click **"Create"**
3. Configure:
   - **Resource group name**: `Cloud-Computing`
   - **Region**: East US
4. Click **"Review + create"** → **"Create"**

#### Step 3: Create an Azure Storage Account

1. Click **"Create a resource"** → Search **"Storage account"**
2. Click **"Create"**
3. Configure:
   - **Resource group**: `Cloud-Computing`
   - **Storage account name**: `clouddataset`
   - **Region**: East US
   - **Redundancy**: LRS (Locally-redundant storage)
4. Click **"Review + create"** → **"Create"**

#### Step 4: Create Blob Containers

1. Navigate to your storage account (`clouddataset`)
2. In the left menu, click **"Containers"** (under Data storage)
3. Click **"+ Container"**
4. Create the following containers:
   - `dataset` — for the input log file
   - `jar-files` — for the MapReduce JAR file
5. Click **"Create"** for each

#### Step 5: Upload Dataset and JAR to Azure Blob Storage

1. Navigate to your storage account (`clouddataset`) → **"Containers"** → `dataset`
2. Click **"Upload"** → Browse and select the `access.log` file → Click **"Upload"**
3. Go back to **"Containers"** → `jar-files`
4. Click **"Upload"** → Browse and select `WebServerLogIntelligence-1.0.jar` → Click **"Upload"**

#### Step 6: Create HDInsight Hadoop Cluster

1. Go to [Azure Portal](https://portal.azure.com)
2. Click **"Create a resource"** → Search **"HDInsight"**
3. Click **"Create"**
4. Configure:
   - **Cluster name**: `serverlog`
   - **Cluster type**: Hadoop
   - **Version**: Hadoop 3.1 (HDInsight 5.x)
   - **Login**: Set username and password
   - **SSH**: Set SSH username and password
   - **Resource group**: `Cloud-Computing`
   - **Location**: East US
5. **Storage**: Select existing storage account `clouddataset`
6. **Cluster size**: 
   - Head nodes: 2 × D12 v2
   - Worker nodes: 2 × D4 v2 (minimum for cost savings)
7. Click **"Create"** (takes ~20 minutes to provision)

---

## Building the Project

```bash
# Navigate to the project directory
cd WebServerLogIntelligence

# Clean and build the project
mvn clean package

```

## Running the MapReduce Jobs

### Option A: Local / Standalone Testing (NO Hadoop Required)

We have included a custom pure-Java `LocalRunner` that simulates the MapReduce process. This allows you to test your MapReduce logic locally on Windows/Mac/Linux **without needing to install Hadoop**!

```powershell
# 1. Build the project and copy dependencies
mvn clean package dependency:copy-dependencies -DoutputDirectory=target/lib -q

# 2. Run the simulation (e.g., running ALL analyses on the sample data)
# Note: You can replace 'all' with 'statuscode', 'topips', 'hourly', etc.
java -cp "target/WebServerLogIntelligence-1.0.jar;target/lib/*" \
  com.weblog.driver.LocalRunner \
  all sample-data/sample_input.log output

# 3. View the generated results
Get-Content output/statuscode/part-r-00000
```

### Option B: Running on Azure HDInsight

#### Step 1: Connect to the cluster via Azure Cloud Shell

Open **Azure Cloud Shell** from the Azure Portal (click the `>_` icon in the top toolbar), then SSH into the cluster:

```bash
ssh sshuser@serverlog-ssh.azurehdinsight.net
```

#### Step 2: Copy the JAR and dataset from Blob Storage to the cluster

> **Note**: This step is done via **Azure Cloud Shell** (accessed from the Azure Portal toolbar).

Open **Azure Cloud Shell** from the Azure Portal (click the `>_` icon in the top toolbar), then run:

```bash
# Create a working directory
mkdir ~/cloud_project && cd ~/cloud_project

# Download the pre-built JAR file from Blob Storage
hdfs dfs -get wasbs://jar-files@clouddataset.blob.core.windows.net/WebServerLogIntelligence-1.0.jar .

# Download the dataset from Blob Storage
hdfs dfs -get wasbs://dataset@clouddataset.blob.core.windows.net/access.log .

# Verify both files
ls -lh
```

#### Step 3: Upload the dataset to HDFS input directory

```bash
hdfs dfs -mkdir -p wasbs:///input
hdfs dfs -put access.log wasbs:///input/access.log
```

#### Step 4: Verify the data is accessible

```bash
# List files in the input directory on Azure Blob Storage (WASBS)
hdfs dfs -ls wasbs:///input/

# Check file size
hdfs dfs -du -h wasbs:///input/access.log
```

#### Step 4: Run individual analyses

```bash
# ---- Analysis 1: HTTP Status Code Distribution ----
hadoop jar WebServerLogIntelligence-1.0.jar \
  statuscode wasbs:///input/access.log wasbs:///output/statuscode

# ---- Analysis 2: Top 10 IP Addresses ----
hadoop jar WebServerLogIntelligence-1.0.jar \
  topips wasbs:///input/access.log wasbs:///output/topips

# ---- Analysis 3: Hourly Traffic Pattern ----
hadoop jar WebServerLogIntelligence-1.0.jar \
  hourly wasbs:///input/access.log wasbs:///output/hourly

# ---- Analysis 4: Top 10 Most Accessed URLs ----
hadoop jar WebServerLogIntelligence-1.0.jar \
  topurls wasbs:///input/access.log wasbs:///output/topurls

# ---- Analysis 5: HTTP Method Distribution ----
hadoop jar WebServerLogIntelligence-1.0.jar \
  httpmethod wasbs:///input/access.log wasbs:///output/httpmethod
```

#### Step 5: Run ALL analyses at once

```bash
hadoop jar WebServerLogIntelligence-1.0.jar \
  all wasbs:///input/access.log wasbs:///output
```

#### Step 6: View the results

```bash
# View Status Code results
hdfs dfs -cat wasbs:///output/statuscode/part-r-00000

# View Top IPs results
hdfs dfs -cat wasbs:///output/topips/part-r-00000

# View Hourly Traffic results
hdfs dfs -cat wasbs:///output/hourly/part-r-00000

# View Top URLs results
hdfs dfs -cat wasbs:///output/topurls/part-r-00000

# View HTTP Method results
hdfs dfs -cat wasbs:///output/httpmethod/part-r-00000
```

#### Step 7: Download results

You can download the output files directly from the Azure Portal:

1. Navigate to your storage account (`clouddataset`) → **"Containers"**
2. Browse to the output container/folder
3. Click on each result folder (e.g., `statuscode/`, `topips/`, etc.)
4. Select the `part-r-00000` file and click **"Download"**

---

## Expected Output

### Analysis 1: HTTP Status Code Distribution
```
200     8547321
301     234567
304     189432
404     67891
403     12345
500     4532
```

### Analysis 2: Top 10 IP Addresses
```
66.249.66.194       543210
54.36.149.41        321456
40.77.167.129       298765
207.46.13.136       187654
31.56.96.51         123456
...
```

### Analysis 3: Hourly Traffic Pattern
```
00:00-00:59     234567
01:00-01:59     198765
...
12:00-12:59     567890
...
23:00-23:59     287654
```

### Analysis 4: Top 10 Most Accessed URLs
```
/image              4567890
/filter             2345678
/product            1234567
/m/filter           987654
/static/images      456789
...
```

### Analysis 5: HTTP Method Distribution
```
GET     9876543
POST    234567
HEAD    12345
PUT     2345
DELETE  678
```

> **Note**: Actual values will vary based on the full dataset.

---

## Results Interpretation

The analysis of the web server access logs from the Iranian ecommerce website (zanbil.ir) reveals several key patterns:

1. **Status Codes**: The vast majority of requests return HTTP 200 (Success), indicating a healthy, well-functioning server. The presence of 404 errors helps identify broken links or removed products. Any 500-series errors highlight server-side issues that need attention.

2. **Top IPs**: The most frequent IP addresses are likely search engine bots (Googlebot, Bingbot, AhrefsBot), which is common for ecommerce websites that need strong SEO presence. This intelligence can help in bot management and crawl budget optimization.

3. **Hourly Traffic**: Traffic patterns reveal peak shopping hours, which is valuable for capacity planning, marketing timing, and server resource allocation.

4. **Popular URLs**: The most accessed URLs show which products and categories are most popular, providing direct business intelligence for inventory management and marketing campaigns.

5. **HTTP Methods**: A dominant GET pattern is expected for an ecommerce browsing experience, with POST requests indicating user interactions (adding to cart, form submissions).

---

## Clean Up Azure Resources

**Important**: Delete resources after completion to avoid ongoing costs.

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to **"Resource groups"** → `Cloud-Computing`
3. Click **"Delete resource group"**
4. Type the resource group name to confirm and click **"Delete"**

> This will delete the HDInsight cluster, storage account, and all associated resources at once.

---

