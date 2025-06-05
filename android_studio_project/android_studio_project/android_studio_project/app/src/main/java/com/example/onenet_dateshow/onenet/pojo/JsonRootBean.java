/**
 * Copyright 2025 json.cn
 */
package com.example.onenet_dateshow.onenet.pojo;

/**
 * Auto-generated: 2025-05-03 19:35:51
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/
 */
public class JsonRootBean {

    private String url;
    private String method;
    private Header header;  // 引用Header类（已单独定义）
    private Query query;    // 引用Query类（已单独定义）
    private String create_time;  // 修正为String类型（匹配"20250502_223232"格式）
    private String resp;
    private int resp_http_code;
    private String api_name;
    private int run_time;
    private String action;
    private String ns;

    // Getter 和 Setter 方法
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Header getHeader() { return header; }
    public void setHeader(Header header) { this.header = header; }

    public Query getQuery() { return query; }
    public void setQuery(Query query) { this.query = query; }

    public String getCreate_time() { return create_time; }
    public void setCreate_time(String create_time) { this.create_time = create_time; }

    public String getResp() { return resp; }
    public void setResp(String resp) { this.resp = resp; }

    public int getResp_http_code() { return resp_http_code; }
    public void setResp_http_code(int resp_http_code) { this.resp_http_code = resp_http_code; }

    public String getApi_name() { return api_name; }
    public void setApi_name(String api_name) { this.api_name = api_name; }

    public int getRun_time() { return run_time; }
    public void setRun_time(int run_time) { this.run_time = run_time; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNs() { return ns; }
    public void setNs(String ns) { this.ns = ns; }
}