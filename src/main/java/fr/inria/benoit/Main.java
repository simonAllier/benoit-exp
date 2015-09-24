package fr.inria.benoit;


import fr.inria.benoit.util.GitUtils;
import fr.inria.benoit.util.Log;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.util.*;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main program
 */
public class Main {

    private int nbProcessor;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.initNbOfProcess();
        main.initGit();

        main.runExp();
    }

    protected  void runExp() throws InterruptedException, GitAPIException, IOException {
        while (true) {
            ExecutorService executor = Executors.newFixedThreadPool(nbProcessor);
            List<String> params = initParameter();

            for(String p : params) {
                final String param = p;
                Runnable runnable = new Runnable() {
                    public void run() {
                        Log.info("run: {}", "sh bin/run_simus.sh /opt/mcr/v80/ " + param);
                        try {
                            Process p = Runtime.getRuntime().exec("sh bin/run_simus.sh /opt/mcr/v80/ " + param);
                            p.waitFor();

                            GitUtils gitUtils = new GitUtils("exp");
                            gitUtils.pull();
                            Log.info("push result");
                            gitUtils.add("results");
                            gitUtils.commit("update");
                            gitUtils.push();
                        } catch (InterruptedException e ) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GitAPIException e) {
                            e.printStackTrace();
                        }
                    }
                };

                executor.execute(runnable);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {   }
        }

    }

    protected void initGit() throws IOException, GitAPIException {
        Log.info("clone the repository https://github.com/simonAllier/benoit-exp-result.git");
        GitUtils gitUtils = new GitUtils("exp");

        gitUtils.cloneRepo();

        File file = new File("exp/results");
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    protected List<String> initParameter() throws InterruptedException, IOException, GitAPIException {
        GitUtils gitUtils = new GitUtils("exp");


        Random r = new Random();
        int sleep = r.nextInt(300);
        Log.info("sleep {} seconds", sleep);
        Thread.sleep(sleep * 1000);
        gitUtils.pull();
        return gitUtils.getPropertiesFiles(10*nbProcessor);
    }


    protected void initNbOfProcess() throws InterruptedException, IOException {
        Runtime r = Runtime.getRuntime();

        Process p = r.exec("cat /proc/cpuinfo");
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("processor"))
                nbProcessor++;
        }
        if(nbProcessor == 0) {
            nbProcessor = 1;
        }
        Log.info("nb of processor: {}", nbProcessor);
        reader.close();

    }

}
