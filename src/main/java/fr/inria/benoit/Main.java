package fr.inria.benoit;


import fr.inria.benoit.util.GitUtils;
import fr.inria.benoit.util.Log;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.util.*;

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
            try {
                List<String> params = initParameter();
                
                for(String param : params) {
                    Log.info("run: {}", "sh bin/run_simus.sh /opt/mcr/v80/ " + param);
                    Process p = Runtime.getRuntime().exec("sh bin/run_simus.sh /opt/mcr/v80/ " + param);
                    p.waitFor();
                }
                
                GitUtils gitUtils = new GitUtils("exp");
                gitUtils.pull();
                Log.info("push result");
                gitUtils.add("results");
                gitUtils.commit("update");
                gitUtils.push();
                
            } catch (Throwable e) {}
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

    protected static List<String> initParameter() throws InterruptedException, IOException, GitAPIException {
        GitUtils gitUtils = new GitUtils("exp");

        Random r = new Random();
        int sleep = r.nextInt(30);
        Log.info("sleep {} seconds", sleep);
        Thread.sleep(sleep * 1000);
        gitUtils.pull();
        return gitUtils.getPropertiesFiles(100);
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
