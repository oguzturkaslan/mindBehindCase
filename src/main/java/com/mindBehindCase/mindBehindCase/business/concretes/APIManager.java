/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindBehindCase.mindBehindCase.business.concretes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindBehindCase.mindBehindCase.business.abstracts.APIService;
import com.mindBehindCase.mindBehindCase.core.utilities.results.ErrorResult;
import com.mindBehindCase.mindBehindCase.core.utilities.results.Result;
import com.mindBehindCase.mindBehindCase.core.utilities.results.SuccessResult;
import com.mindBehindCase.mindBehindCase.model.Comments;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

/**
 *
 * @author oguz.turkaslan
 */
@Service
public class APIManager implements APIService {

    @Override
    public Result getComments() {
        List<Comments> commentsList = new ArrayList<>();
        Gson gson = new Gson();
        Result requestAPI = requestHTTP();
        if (requestAPI.getData().equals("error")) {
            return new ErrorResult(false, requestAPI.getErrorCode(), requestAPI.getMessage());
        } else if (requestAPI.getErrorCode() > HttpURLConnection.HTTP_OK) {
            return new ErrorResult(false, requestAPI.getErrorCode(), requestAPI.getMessage());
        } else {
            String commentsJson = (String) requestAPI.getData();
            commentsList = gson.fromJson(commentsJson, new TypeToken<List<Comments>>() {
            }.getType());
            FileWriter writer;
            try {
                writer = new FileWriter("output.txt");
                for (Comments comment : commentsList) {
                    writer.write(comment.getId() + ":" + comment.getBody() + ", ");
                }
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(APIManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new SuccessResult(true);
        }

    }

    public Result requestHTTP() {
        URL url;
        HttpURLConnection con = null;
        StringBuilder sb = null;
        InputStream is;
        BufferedReader br;
        Result result = null;
        try {
            url = new URL("https://my-json-server.typicode.com/typicode/demo/comments");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            sb = new StringBuilder();
            String line;

            if (con.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                is = con.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                result = new Result(sb.toString(), con.getResponseCode(), con.getResponseMessage());
                br.close();
            } else {
                is = con.getErrorStream();
                br = new BufferedReader(new InputStreamReader(is));
                sb.append("{")
                        .append("\"errorCode\"" + ":")
                        .append(con.getResponseCode())
                        .append(",")
                        .append("\"message\"" + ":")
                        .append("\"" + con.getResponseMessage() + "\"")
                        .append("}");

                result = new Result(sb.toString(), con.getResponseCode(), con.getResponseMessage());
            }

        } catch (IOException ex) {
            result = new Result("error", 0, "Connection Error");
            Logger.getLogger(APIManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
