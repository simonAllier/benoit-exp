package fr.inria.benoit.util;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.util.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Simon on 29/08/14.
 */
public class GitUtils {
    protected String remotePath = "https://github.com/simonAllier/benoit-exp-result.git";
    protected String localPath;
    protected Repository localRepository;
    protected Git git;


    protected String user = "diversify-exp-user";
    protected String password = "diversify-exp-password";



    public GitUtils(String localPath) throws IOException, GitAPIException {
        this.localPath = localPath;

        localRepository = new FileRepository(localPath + "/.git");
        git = new Git(localRepository);
    }

    public void cloneRepo() throws GitAPIException, IOException {
        File localDir = new File(localPath);
        if(localDir.exists())
            FileUtils.forceDelete(localDir);

        Git.cloneRepository().setURI(remotePath)
           .setDirectory(localDir).call();
    }

    public void push() throws GitAPIException {
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password)).call();
    }

    public void pull() throws GitAPIException {
        git.pull().call();
    }

    public void add(String filePattern) throws GitAPIException {
        git.add().addFilepattern(filePattern).call();
    }

    public String getFirstPropertyFile() throws IOException, GitAPIException {
        Set<String> done = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(localPath + "/exp"));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        String ret = null;
        while (line != null) {
            if(ret == null && !line.endsWith("OK") && !done.contains(line+" OK")) {
                ret = line;
                sb.append(line+" OK\n");
            }
            else {
                done.add(line);
                sb.append(line + "\n");
            }
            line = br.readLine();
        }
        Log.info("properties file: {}",ret);
        updateExpList(sb.toString());
        return ret;
    }

    public List<String> getPropertiesFiles(int nb)  throws IOException, GitAPIException {
        Set<String> done = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(localPath + "/exp"));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        
        List<String> ret = new ArrayList<>(nb);
        int count = 0;
        while (line != null && count < nb) {
            if(!line.endsWith("OK")) {
                ret.add(line);
                sb.append(line+" OK\n");
                count++;
            }
            else {
                done.add(line);
                sb.append(line + "\n");
            }
            line = br.readLine();
        }
        
        Log.info("properties file: {}",ret);
        updateExpList(sb.toString());
        return ret;

    }
    
    public void commit(String message) throws GitAPIException {
        git.commit().setMessage(message).call();
    }

    private void updateExpList(String s) throws IOException, GitAPIException {
        BufferedWriter out = new BufferedWriter(new FileWriter(localPath + "/exp"));
        pull();
        out.write(s);
        out.close();
        add("exp");
        commit("update");
        push();
    }
}
