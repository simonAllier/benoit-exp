package fr.inria.benoit;


import fr.inria.benoit.util.GitUtils;
import fr.inria.benoit.util.Log;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.util.Random;

/**
 * Main program
 */
public class Main {

    public static void main(String[] args) throws Exception {

        if(args.length != 0) {
            try {
                initNbOfProcess();
                initGit();
            } catch (Exception e) {
                Log.error("Main ", e);
            }
        }
        else {
            runExp();
        }
    }

    protected static void runExp() throws IOException, InterruptedException, GitAPIException {
        while (true) {
            String param = initParameter();
            Log.info("run: {}", "sh bin/run_simus.sh /opt/mcr/v80/ " + param);
            Process p = Runtime.getRuntime().exec("sh bin/run_simus.sh /opt/mcr/v80/ " + param);
            p.waitFor();
            GitUtils gitUtils = new GitUtils("exp");
            gitUtils.pull();
            String[] split = param.split(" ");
            String pp = split[0] + "_" + split[1] + "_" + split[2] + "_" + split[3];
            gitUtils.add("results/biom_" + pp + ".txt");
            gitUtils.add("results/ramets_" + pp + ".txt");
            gitUtils.commit("update");
            gitUtils.push();
        }

    }

    protected static void initGit() throws IOException, GitAPIException {
        Log.info("clone the repository https://github.com/simonAllier/benoit-exp-result.git");
        GitUtils gitUtils = new GitUtils("exp");

        gitUtils.cloneRepo();

        File file = new File("exp/results");
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    protected static String initParameter() throws InterruptedException, IOException, GitAPIException {
        GitUtils gitUtils = new GitUtils("exp");

        Random r = new Random();
        int sleep = r.nextInt(100);
        Log.info("sleep {} seconds", sleep);
        Thread.sleep(sleep * 1000);

        return gitUtils.getFirstPropertyFile();
    }


    protected static void initNbOfProcess() throws InterruptedException, IOException {
        Runtime r = Runtime.getRuntime();

        Process p = r.exec("cat /proc/cpuinfo");
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        int i = 0;
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("processor"))
                i++;
        }
        reader.close();
        BufferedWriter out = new BufferedWriter(new FileWriter("nbProcess"));
        out.write(i+"");
        out.close();
    }

}
