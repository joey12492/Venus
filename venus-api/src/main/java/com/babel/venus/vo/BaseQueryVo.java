package com.babel.venus.vo;

import lombok.Data;

import java.util.List;

/**
 * User: joey
 * Date: 2017/9/18
 * Time: 17:51
 */
@Data
public class BaseQueryVo {

    private Long startTime;
    private Long endTime;
    private Long pcode;
    private Integer pdate;
    private Integer orderStatus;
    private Integer ifChase;
    private Integer chaseCount;
    private Integer chaseWinStop;
    private List<String> orderIds;
    private String parentOrderId;
    private String winNumber;
    private Long lotteryId;
    private Page page;

    private class Page {
        private Integer pageNum;
        private Integer size;

        public Integer getStart() {
            return (pageNum - 1) * size;
        }

        public Integer getSize() {
            return size;
        }

        public Page(Integer pageNum, Integer size) {
            this.pageNum = pageNum;
            this.size = size;
        }
    }

    public void setPage(Integer pageNum, Integer size) {
        page = new Page(pageNum, size);
    }

    public Page getPage() {
        return page;
    }

    public Integer getStart() {
        return (page.pageNum - 1) * page.size;
    }

    public Integer getSize() {
        return page.size;
    }

    public BaseQueryVo() {
    }

    public BaseQueryVo(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public BaseQueryVo(Long startTime, Long endTime, Long pcode) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.pcode = pcode;
    }

    public BaseQueryVo(Long startTime, Long endTime, Long pcode, Integer pdate) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.pcode = pcode;
        this.pdate = pdate;
    }

    public BaseQueryVo(Long pcode, Integer ifChase, Integer orderStatus, Integer pdate, Integer chaseCount) {
        this.pcode = pcode;
        this.ifChase = ifChase;
        this.orderStatus = orderStatus;
        this.pdate = pdate;
        this.chaseCount = chaseCount;
    }


}
