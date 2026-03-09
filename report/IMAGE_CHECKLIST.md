# Report — Image Checklist

Place all screenshots in the `images/` folder with **exactly** these filenames (PNG recommended).
The LaTeX document references each one; a missing image will cause a compilation warning.

## Required Images

| # | Filename | What to capture |
|---|----------|-----------------|
| 1 | `uor-logo.png` | University of Ruhuna logo (or Faculty of Engineering crest). Find from the university website or your student portal. |
| 2 | `hdinsight-cluster-overview.png` | Azure Portal → HDInsight cluster **Overview** blade showing cluster name, status, region, Hadoop version, node count, etc. |
| 3 | `hdinsight-resource-group.png` | Azure Portal → Resource Group view listing all resources (Storage Account, HDInsight cluster, etc.). |
| 4 | `maven-build-success.png` | Terminal output of `mvn clean package` showing **BUILD SUCCESS**. |
| 5 | `blob-storage-upload.png` | Azure Portal → Storage Account → Container → showing `input/access.log` uploaded (or `az storage blob upload` CLI output). |
| 6 | `scp-jar-upload.png` | Terminal showing `scp WebServerLogIntelligence-1.0.jar sshuser@…` command completing successfully. |
| 7 | `ssh-hadoop-jar-run.png` | SSH session on HDInsight head node running `hadoop jar WebServerLogIntelligence-1.0.jar all wasbs:///input/access.log wasbs:///output`. Capture the initial job launch messages. |
| 8 | `job-completion-summary.png` | The final output after all 5 jobs finish: "Completed: 5/5 analyses successful". |
| 9 | `hdfs-cat-statuscode.png` | Output of `hdfs dfs -cat wasbs:///output/statuscode/part-r-00000`. |
| 10 | `hdfs-cat-topips.png` | Output of `hdfs dfs -cat wasbs:///output/topips/part-r-00000`. |
| 11 | `hdfs-cat-hourly.png` | Output of `hdfs dfs -cat wasbs:///output/hourly/part-r-00000`. |
| 12 | `hdfs-cat-topurls.png` | Output of `hdfs dfs -cat wasbs:///output/topurls/part-r-00000`. |
| 13 | `hdfs-cat-httpmethod.png` | Output of `hdfs dfs -cat wasbs:///output/httpmethod/part-r-00000`. |
| 14 | `ambari-dashboard.png` | Ambari web UI dashboard (usually at `https://<cluster-name>.azurehdinsight.net`) showing cluster health, services, and metrics. |
| 15 | `yarn-completed-jobs.png` | YARN Resource Manager UI → Applications page showing the 5 completed MapReduce jobs. You can reach it via Ambari → YARN → Quick Links → Resource Manager UI. |

## How to compile the report

```bash
# Option 1: pdflatex (run twice for references)
pdflatex report.tex
pdflatex report.tex

# Option 2: latexmk (automated)
latexmk -pdf report.tex
```

Or use **Overleaf**: upload the entire `report/` folder (including `images/`) as a project.

## Tips
- Crop screenshots to remove unnecessary browser chrome / taskbar.
- If an image is very wide, use a 16:9 ratio crop.
- Aim for at least 150 DPI so text in screenshots remains readable when printed.
