/**
 * @author Taehyun Park
 */

package controllers;

import java.io.File;
import java.util.List;

import com.avaje.ebean.Page;

import models.Issue;
import models.IssueComment;
import models.Milestone;
import models.User;
import models.Project;
import models.enumeration.Direction;
import models.enumeration.IssueState;
import models.enumeration.IssueStateType;
import models.support.SearchCondition;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.issue.issue;
import views.html.issue.issueList;
import views.html.issue.newIssue;
import views.html.issue.notExistingPage;

public class IssueApp extends Controller {

    /**
     * 페이지 처리된 이슈들의 리스트를 보여준다.
     * 
     * @param projectName
     *            프로젝트 이름
     * @param pageNum
     *            보여줄 페이지 번호
     * @param sortBy
     *            이슈 정렬 기준(Column)
     * @param order
     *            이슈 정렬 방향(오름차순, 내림차순)
     * @param filter
     *            이슈 제목에 적용될 검색 필터
     * @param status
     *            이슈 해결 상태
     * @param commentedCheck
     *            이슈 댓글 달림 유무
     * @param fileAttachedheck
     *            이슈 파일 첨부 유무
     * @return
     */
    public static Result list(String projectName, String stateType) {
        Project project = Project.findByName(projectName);
        Form<SearchCondition> issueParamForm = new Form<SearchCondition>(
                SearchCondition.class);
        SearchCondition issueParam = issueParamForm.bindFromRequest().get();
        Logger.debug("IssueApp : list - issueParam.sortBy = " + issueParam.sortBy);
        Logger.debug("IssueApp : list - issueParam.orderBy = " + Direction.getValue(issueParam.orderBy).direction());
        if (project == null) {
            return notFound();
        }
        Page<Issue> issues = Issue.findIssues(project.name, issueParam.pageNum,
                IssueStateType.getValue(stateType), issueParam.sortBy,
                Direction.getValue(issueParam.orderBy), issueParam.filter,
                issueParam.commentedCheck, issueParam.fileAttachedCheck);

        return ok(issueList.render("이슈 목록", issues, issueParam, project));
    }

    public static Result issue(String projectName, Long issueId) {
        Project project = Project.findByName(projectName);
        if (project == null) {
            return notFound();
        }
        Issue issues = Issue.findById(issueId);
        List<IssueComment> comments = IssueComment
                .findCommentsByIssueId(issueId);
        if (issues == null) {
            return ok(notExistingPage.render("존재하지 않는 게시물", project));
        } else {
            Form<IssueComment> commentForm = new Form<IssueComment>(
                    IssueComment.class);
            return ok(issue.render("이슈 상세조회", issues, comments, commentForm,
                    project));
        }
    }

    public static Result newIssue(String projectName) {
        Project project = Project.findByName(projectName);
        if (project == null) {
            return notFound();
        }
        return ok(newIssue
                .render("새 이슈", new Form<Issue>(Issue.class), project));
    }

    public static Result saveIssue(String projectName) {
        Form<Issue> issueForm = new Form<Issue>(Issue.class).bindFromRequest();
        Project project = Project.findByName(projectName);

        if (project == null) {
            return notFound();
        }
        if (issueForm.hasErrors()) {
            return badRequest(newIssue.render(issueForm.errors().toString(),
                    issueForm, project));
        } else {
            Issue newIssue = issueForm.get();
            newIssue.reporterId = UserApp.currentUser().id;
            newIssue.project = project;
            // TODO 추후에 초기값(미분류된 이슈를 담는 마일스톤)으로 연결
            // newIssue.milestone = Milestone.findById(1l);
            newIssue.state = IssueState.ENROLLED;
            newIssue.updateStatusType(newIssue.state);
            newIssue.filePath = saveFile(request());
            Issue.create(newIssue);
        }
        return redirect(routes.IssueApp.list(project.name,
                IssueStateType.ALL.stateType()));
    }

    public static Result delete(String projectName, Long issueId) {
        Project project = Project.findByName(projectName);
        if (project == null) {
            return notFound();
        }
        Issue.delete(issueId);
        return redirect(routes.IssueApp.list(project.name,
                IssueStateType.ALL.stateType()));
    }

    public static Result saveComment(String projectName, Long issueId) {
        Form<IssueComment> commentForm = new Form<IssueComment>(
                IssueComment.class).bindFromRequest();

        Project project = Project.findByName(projectName);
        if (commentForm.hasErrors()) {
            return TODO;

        } else {
            IssueComment comment = commentForm.get();
            comment.issue = Issue.findById(issueId);
            comment.authorId = UserApp.currentUser().id;
            comment.filePath = saveFile(request());
            IssueComment.create(comment);

            return redirect(routes.IssueApp.issue(project.name, issueId));
        }
    }

    public static Result extractExcelFile(String projectName) {
        return TODO;
    }

    /**
     * From BoardApp
     * 
     * @param request
     * @return
     */
    private static String saveFile(Request request) {
        MultipartFormData body = request.body().asMultipartFormData();

        FilePart filePart = body.getFile("filePath");

        if (filePart != null) {
            File saveFile = new File("public/uploadFiles/"
                    + filePart.getFilename());
            filePart.getFile().renameTo(saveFile);
            return filePart.getFilename();
        }
        return null;
    }
}
