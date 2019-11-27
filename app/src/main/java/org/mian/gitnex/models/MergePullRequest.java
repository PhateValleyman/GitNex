package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class MergePullRequest {

    private String Do;
    private String MergeMessageField;
    private String MergeTitleField;

    public MergePullRequest(MergePullRequest.Mode mode, String MergeMessageField, String MergeTitleField) {
        switch (mode) {
            case Squash:
                this.Do = "squash";
                break;
            case Rebase:
                this.Do = "rebase";
                break;
            case RebaseMerge:
                this.Do = "rebase-merge";
                break;
            case Merge:
            default:
                this.Do = "merge";
        }
        this.MergeMessageField = MergeMessageField;
        this.MergeTitleField = MergeTitleField;
    }

    public enum Mode{
        Merge,          //merge
        Rebase,         //rebase
        RebaseMerge,    //rebase-merge
        Squash,         //squash
    }

}
