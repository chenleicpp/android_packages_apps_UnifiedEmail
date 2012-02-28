/*******************************************************************************
 *      Copyright (C) 2011 Google Inc.
 *      Licensed to The Android Open Source Project.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *******************************************************************************/

package com.android.mail.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.net.Uri;

import com.android.common.contacts.DataUsageStatUpdater;

import java.lang.String;
import java.util.ArrayList;


public class UIProvider {
    public static final String EMAIL_SEPARATOR = "\n";
    public static final long INVALID_CONVERSATION_ID = -1;
    public static final long INVALID_MESSAGE_ID = -1;

    /**
     * Values for the current state of a Folder/Account; note that it's possible that more than one
     * sync is in progress
     */
    public static final class SyncStatus {
        // No sync in progress
        public static final int NO_SYNC = 0;
        // A user-requested sync/refresh is in progress
        public static final int USER_REFRESH = 1<<0;
        // A user-requested query is in progress
        public static final int USER_QUERY = 1<<1;
        // A user request for additional results is in progress
        public static final int USER_MORE_RESULTS = 1<<2;
        // A background sync is in progress
        public static final int BACKGROUND_SYNC = 1<<3;
    }

    /**
     * Values for the result of the last attempted sync of a Folder/Account
     */
    public static final class LastSyncResult {
        // The sync completed successfully
        public static final int SUCCESS = 0;
        // The sync wasn't completed due to a connection error
        public static final int CONNECTION_ERROR = 1;
        // The sync wasn't completed due to an authentication error
        public static final int AUTH_ERROR = 2;
        // The sync wasn't completed due to a security error
        public static final int SECURITY_ERROR = 3;
        // The sync wasn't completed due to a low memory condition
        public static final int STORAGE_ERROR = 4;
        // The sync wasn't completed due to an internal error/exception
        public static final int INTERNAL_ERROR = 5;
    }

    // The actual content provider should define its own authority
    public static final String AUTHORITY = "com.android.mail.providers";

    public static final String ACCOUNT_LIST_TYPE =
            "vnd.android.cursor.dir/vnd.com.android.mail.account";
    public static final String ACCOUNT_TYPE =
            "vnd.android.cursor.item/vnd.com.android.mail.account";

    public static final String[] ACCOUNTS_PROJECTION = {
            BaseColumns._ID,
            AccountColumns.NAME,
            AccountColumns.PROVIDER_VERSION,
            AccountColumns.URI,
            AccountColumns.CAPABILITIES,
            AccountColumns.FOLDER_LIST_URI,
            AccountColumns.SEARCH_URI,
            AccountColumns.ACCOUNT_FROM_ADDRESSES_URI,
            AccountColumns.SAVE_DRAFT_URI,
            AccountColumns.SEND_MAIL_URI,
            AccountColumns.EXPUNGE_MESSAGE_URI,
            AccountColumns.UNDO_URI,
            AccountColumns.SETTINGS_INTENT_URI,
            AccountColumns.SETTINGS_QUERY_URI,
            AccountColumns.SYNC_STATUS,
            AccountColumns.HELP_INTENT_URI,
            AccountColumns.COMPOSE_URI
    };

    public static final int ACCOUNT_ID_COLUMN = 0;
    public static final int ACCOUNT_NAME_COLUMN = 1;
    public static final int ACCOUNT_PROVIDER_VERISON_COLUMN = 2;
    public static final int ACCOUNT_URI_COLUMN = 3;
    public static final int ACCOUNT_CAPABILITIES_COLUMN = 4;
    public static final int ACCOUNT_FOLDER_LIST_URI_COLUMN = 5;
    public static final int ACCOUNT_SEARCH_URI_COLUMN = 6;
    public static final int ACCOUNT_FROM_ADDRESSES_URI_COLUMN = 7;
    public static final int ACCOUNT_SAVE_DRAFT_URI_COLUMN = 8;
    public static final int ACCOUNT_SEND_MESSAGE_URI_COLUMN = 9;
    public static final int ACCOUNT_EXPUNGE_MESSAGE_URI_COLUMN = 10;
    public static final int ACCOUNT_UNDO_URI_COLUMN = 11;
    public static final int ACCOUNT_SETTINGS_INTENT_URI_COLUMN = 12;
    public static final int ACCOUNT_SETTINGS_QUERY_URI_COLUMN = 13;
    public static final int ACCOUNT_SYNC_STATUS_COLUMN = 14;
    public static final int ACCOUNT_HELP_INTENT_URI_COLUMN = 15;
    public static final int ACCOUNT_COMPOSE_INTENT_URI_COLUMN = 16;

    public static final class AccountCapabilities {
        /**
         * Whether folders can be synchronized back to the server.
         */
        public static final int SYNCABLE_FOLDERS = 0x0001;
        /**
         * Whether the server allows reporting spam back.
         */
        public static final int REPORT_SPAM = 0x0002;
        /**
         * Whether the server supports a concept of Archive: removing mail from the Inbox but
         * keeping it around.
         */
        public static final int ARCHIVE = 0x0004;
        /**
         * Whether the server will stop notifying on updates to this thread? This requires
         * THREADED_CONVERSATIONS to be true, otherwise it should be ignored.
         */
        public static final int MUTE = 0x0008;
        /**
         * Whether the server supports searching over all messages. This requires SYNCABLE_FOLDERS
         * to be true, otherwise it should be ignored.
         */
        public static final int SERVER_SEARCH = 0x0010;
        /**
         * Whether the server supports constraining search to a single folder. Requires
         * SYNCABLE_FOLDERS, otherwise it should be ignored.
         */
        public static final int FOLDER_SERVER_SEARCH = 0x0020;
        /**
         * Whether the server sends us sanitized HTML (guaranteed to not contain malicious HTML).
         */
        public static final int SANITIZED_HTML = 0x0040;
        /**
         * Whether the server allows synchronization of draft messages. This does NOT require
         * SYNCABLE_FOLDERS to be set.
         */
        public static final int DRAFT_SYNCHRONIZATION = 0x0080;
        /**
         * Does the server allow the user to compose mails (and reply) using addresses other than
         * their account name? For instance, GMail allows users to set FROM addresses that are
         * different from account@gmail.com address. For instance, user@gmail.com could have another
         * FROM: address like user@android.com. If the user has enabled multiple FROM address, he
         * can compose (and reply) using either address.
         */
        public static final int MULTIPLE_FROM_ADDRESSES = 0x0100;
        /**
         * Whether the server allows the original message to be included in the reply by setting a
         * flag on the reply. If we can avoid including the entire previous message, we save on
         * bandwidth (replies are shorter).
         */
        public static final int SMART_REPLY = 0x0200;
        /**
         * Does this account support searching locally, on the device? This requires the backend
         * storage to support a mechanism for searching.
         */
        public static final int LOCAL_SEARCH = 0x0400;
        /**
         * Whether the server supports a notion of threaded conversations: where replies to messages
         * are tagged to keep conversations grouped. This could be full threading (each message
         * lists its parent) or conversation-level threading (each message lists one conversation
         * which it belongs to)
         */
        public static final int THREADED_CONVERSATIONS = 0x0800;
        /**
         * Whether the server supports allowing a conversation to be in multiple folders. (Or allows
         * multiple labels on a single conversation, since labels and folders are interchangeable
         * in this application.)
         */
        public static final int MULTIPLE_FOLDERS_PER_CONV = 0x1000;
        /**
         * Whether the provider supports undoing operations. If it doesn't, never show the undo bar.
         */
        public static final int UNDO = 0x2000;
        /**
         * Whether the account provides help content.
         */
        public static final int HELP_CONTENT = 0x4000;
    }

    public static final class AccountColumns {
        /**
         * This string column contains the human visible name for the account.
         */
        public static final String NAME = "name";

        /**
         * This integer column returns the version of the UI provider schema from which this
         * account provider will return results.
         */
        public static final String PROVIDER_VERSION = "providerVersion";

        /**
         * This string column contains the uri to directly access the information for this account.
         */
        public static final String URI = "accountUri";

        /**
         * This integer column contains a bit field of the possible cabibilities that this account
         * supports.
         */
        public static final String CAPABILITIES = "capabilities";

        /**
         * This string column contains the content provider uri to return the
         * list of top level folders for this account.
         */
        public static final String FOLDER_LIST_URI = "folderListUri";

        /**
         * This string column contains the content provider uri that can be queried for search
         * results.
         */
        public static final String SEARCH_URI = "searchUri";

        /**
         * This string column contains the content provider uri that can be queried to access the
         * from addresses for this account.
         */
        public static final String ACCOUNT_FROM_ADDRESSES_URI = "accountFromAddressesUri";

        /**
         * This string column contains the content provider uri that can be used to save (insert)
         * new draft messages for this account. NOTE: This might be better to
         * be an update operation on the messageUri.
         */
        public static final String SAVE_DRAFT_URI = "saveDraftUri";

        /**
         * This string column contains the content provider uri that can be used to send
         * a message for this account.
         * NOTE: This might be better to be an update operation on the messageUri.
         */
        public static final String SEND_MAIL_URI = "sendMailUri";

        /**
         * This string column contains the content provider uri that can be used
         * to expunge a message from this account. NOTE: This might be better to
         * be an update operation on the messageUri.
         */
        public static final String EXPUNGE_MESSAGE_URI = "expungeMessageUri";

        /**
         * This string column contains the content provider uri that can be used
         * to undo the last committed action.
         */
        public static final String UNDO_URI = "undoUri";

        /**
         * Uri for EDIT intent that will cause the settings screens for this account type to be
         * shown.
         * TODO: When we want to support a heterogeneous set of account types, this value may need
         * to be moved to a global content provider.
         */
        public static String SETTINGS_INTENT_URI = "accountSettingsIntentUri";

        /**
         * This string column contains the content provider uri that can be used to query user
         * settings/preferences.
         *
         * The cursor returned by this query support columnms declared in {@link #SettingsColumns}
         */
        public static final String SETTINGS_QUERY_URI = "accountSettingsQueryUri";

        /**
         * Uri for VIEW intent that will cause the help screens for this account type to be
         * shown.
         * TODO: When we want to support a heterogeneous set of account types, this value may need
         * to be moved to a global content provider.
         */
        public static String HELP_INTENT_URI = "helpIntentUri";

        /**
         * This int column contains the current sync status of the account (the logical AND of the
         * sync status of folders in this account)
         */
        public static final String SYNC_STATUS = "syncStatus";
        /**
         * Uri for VIEW intent that will cause the compose screens for this type
         * of account to be shown.
         */
        public static final String COMPOSE_URI = "composeUri";
    }

    // We define a "folder" as anything that contains a list of conversations.
    public static final String FOLDER_LIST_TYPE =
            "vnd.android.cursor.dir/vnd.com.android.mail.folder";
    public static final String FOLDER_TYPE =
            "vnd.android.cursor.item/vnd.com.android.mail.folder";

    public static final String[] FOLDERS_PROJECTION = {
        BaseColumns._ID,
        FolderColumns.URI,
        FolderColumns.NAME,
        FolderColumns.HAS_CHILDREN,
        FolderColumns.CAPABILITIES,
        FolderColumns.SYNC_WINDOW,
        FolderColumns.CONVERSATION_LIST_URI,
        FolderColumns.CHILD_FOLDERS_LIST_URI,
        FolderColumns.UNREAD_COUNT,
        FolderColumns.TOTAL_COUNT,
        FolderColumns.REFRESH_URI,
        FolderColumns.SYNC_STATUS,
        FolderColumns.LAST_SYNC_RESULT
    };

    public static final int FOLDER_ID_COLUMN = 0;
    public static final int FOLDER_URI_COLUMN = 1;
    public static final int FOLDER_NAME_COLUMN = 2;
    public static final int FOLDER_HAS_CHILDREN_COLUMN = 3;
    public static final int FOLDER_CAPABILITIES_COLUMN = 4;
    public static final int FOLDER_SYNC_WINDOW_COLUMN = 5;
    public static final int FOLDER_CONVERSATION_LIST_URI_COLUMN = 6;
    public static final int FOLDER_CHILD_FOLDERS_LIST_COLUMN = 7;
    public static final int FOLDER_UNREAD_COUNT_COLUMN = 8;
    public static final int FOLDER_TOTAL_COUNT_COLUMN = 9;
    public static final int FOLDER_REFRESH_URI_COLUMN = 10;
    public static final int FOLDER_SYNC_STATUS_COLUMN = 11;
    public static final int FOLDER_LAST_SYNC_RESULT_COLUMN = 12;

    public static final class FolderCapabilities {
        public static final int SYNCABLE = 0x0001;
        public static final int PARENT = 0x0002;
        public static final int CAN_HOLD_MAIL = 0x0004;
        public static final int CAN_ACCEPT_MOVED_MESSAGES = 0x0008;
        /**
         * For accounts that support archive, this will indicate that this folder supports
         * the archive functionality.
         */
        public static final int ARCHIVE = 0x0010;

        /**
         * For accounts that support report spam, this will indicate that this folder supports
         * the report spam functionality.
         */
        public static final int REPORT_SPAM = 0x0020;

        /**
         * For accounts that support mute, this will indicate if a mute is performed from within
         * this folder, the action is destructive.
         */
        public static final int DESTRUCTIVE_MUTE = 0x0040;
    }

    public static final class FolderColumns {
        public static final String URI = "folderUri";
        /**
         * This string column contains the human visible name for the folder.
         */
        public static final String NAME = "name";
        /**
         * This int column represents the capabilities of the folder specified by
         * FolderCapabilities flags.
         */
        public static String CAPABILITIES = "capabilities";
        /**
         * This int column represents whether or not this folder has any
         * child folders.
         */
        public static String HAS_CHILDREN = "hasChildren";
        /**
         * This int column represents how large the sync window is.
         */
        public static String SYNC_WINDOW = "syncWindow";
        /**
         * This string column contains the content provider uri to return the
         * list of conversations for this folder.
         */
        public static final String CONVERSATION_LIST_URI = "conversationListUri";
        /**
         * This string column contains the content provider uri to return the
         * list of child folders of this folder.
         */
        public static final String CHILD_FOLDERS_LIST_URI = "childFoldersListUri";

        public static final String UNREAD_COUNT = "unreadCount";

        public static final String TOTAL_COUNT = "totalCount";
        /**
         * This string column contains the content provider uri to force a
         * refresh of this folder.
         */
        public static final  String REFRESH_URI = "refreshUri";
        /**
         * This int column contains current sync status of the folder; some combination of the
         * SyncStatus bits defined above
         */
        public static final String SYNC_STATUS  = "syncStatus";
        /**
         * This int column contains the sync status of the last sync attempt; one of the
         * LastSyncStatus values defined above
         */
        public static final String LAST_SYNC_RESULT  = "lastSyncResult";

        public FolderColumns() {}
    }

    // We define a "folder" as anything that contains a list of conversations.
    public static final String CONVERSATION_LIST_TYPE =
            "vnd.android.cursor.dir/vnd.com.android.mail.conversation";
    public static final String CONVERSATION_TYPE =
            "vnd.android.cursor.item/vnd.com.android.mail.conversation";


    public static final String[] CONVERSATION_PROJECTION = {
        BaseColumns._ID,
        ConversationColumns.URI,
        ConversationColumns.MESSAGE_LIST_URI,
        ConversationColumns.SUBJECT,
        ConversationColumns.SNIPPET,
        ConversationColumns.SENDER_INFO,
        ConversationColumns.DATE_RECEIVED_MS,
        ConversationColumns.HAS_ATTACHMENTS,
        ConversationColumns.NUM_MESSAGES,
        ConversationColumns.NUM_DRAFTS,
        ConversationColumns.SENDING_STATE,
        ConversationColumns.PRIORITY,
        ConversationColumns.READ,
        ConversationColumns.STARRED,
        ConversationColumns.FOLDER_LIST
    };

    // These column indexes only work when the caller uses the
    // default CONVERSATION_PROJECTION defined above.
    public static final int CONVERSATION_ID_COLUMN = 0;
    public static final int CONVERSATION_URI_COLUMN = 1;
    public static final int CONVERSATION_MESSAGE_LIST_URI_COLUMN = 2;
    public static final int CONVERSATION_SUBJECT_COLUMN = 3;
    public static final int CONVERSATION_SNIPPET_COLUMN = 4;
    public static final int CONVERSATION_SENDER_INFO_COLUMN = 5;
    public static final int CONVERSATION_DATE_RECEIVED_MS_COLUMN = 6;
    public static final int CONVERSATION_HAS_ATTACHMENTS_COLUMN = 7;
    public static final int CONVERSATION_NUM_MESSAGES_COLUMN = 8;
    public static final int CONVERSATION_NUM_DRAFTS_COLUMN = 9;
    public static final int CONVERSATION_SENDING_STATE_COLUMN = 10;
    public static final int CONVERSATION_PRIORITY_COLUMN = 11;
    public static final int CONVERSATION_READ_COLUMN = 12;
    public static final int CONVERSATION_STARRED_COLUMN = 13;
    public static final int CONVERSATION_FOLDER_LIST_COLUMN = 14;

    public static final class ConversationSendingState {
        public static final int OTHER = 0;
        public static final int SENDING = 1;
        public static final int SENT = 2;
        public static final int SEND_ERROR = -1;
    }

    public static final class ConversationPriority {
        public static final int LOW = 0;
        public static final int HIGH = 1;
    }

    public static final class ConversationFlags {
        public static final int READ = 1<<0;
        public static final int STARRED = 1<<1;
        public static final int REPLIED = 1<<2;
        public static final int FORWARDED = 1<<3;
    }

    public static final class ConversationColumns {
        public static final String URI = "conversationUri";
        /**
         * This string column contains the content provider uri to return the
         * list of messages for this conversation.
         */
        public static final String MESSAGE_LIST_URI = "messageListUri";
        /**
         * This string column contains the subject string for a conversation.
         */
        public static final String SUBJECT = "subject";
        /**
         * This string column contains the snippet string for a conversation.
         */
        public static final String SNIPPET = "snippet";
        /**
         * This string column contains the sender info string for a
         * conversation.
         */
        public static final String SENDER_INFO = "senderInfo";
        /**
         * This long column contains the time in ms of the latest update to a
         * conversation.
         */
        public static final String DATE_RECEIVED_MS = "dateReceivedMs";

        /**
         * This boolean column contains whether any messages in this conversation
         * have attachments.
         */
        public static final String HAS_ATTACHMENTS = "hasAttachments";

        /**
         * This int column contains the number of messages in this conversation.
         * For unthreaded, this will always be 1.
         */
        public static String NUM_MESSAGES = "numMessages";

        /**
         * This int column contains the number of drafts associated with this
         * conversation.
         */
        public static String NUM_DRAFTS = "numDrafts";

        /**
         * This int column contains the state of drafts and replies associated
         * with this conversation. Use ConversationSendingState to interpret
         * this field.
         */
        public static String SENDING_STATE = "sendingState";

        /**
         * This int column contains the priority of this conversation. Use
         * ConversationPriority to interpret this field.
         */
        public static String PRIORITY = "priority";

        /**
         * This boolean column indicates whether the conversation has been read
         */
        public static String READ = "read";

        /**
         * This boolean column indicates whether the conversation has been read
         */
        public static String STARRED = "starred";

        /**
         * This string column contains a csv of all folders associated with this
         * conversation
         */
        public static final String FOLDER_LIST = "folderList";

        private ConversationColumns() {
        }
    }

    /**
     * List of operations that can can be performed on a conversation. These operations are applied
     * with {@link ContentProvider#update(Uri, ContentValues, String, String[])}
     * where the conversation uri is specified, and the ContentValues specifies the operation to
     * be performed.
     * <p/>
     * The operation to be performed is specified in the ContentValues by
     * the {@link ConversationOperations#OPERATION_KEY}
     * <p/>
     * Note not all UI providers will support these operations.  {@link AccountCapabilities} can
     * be used to determine which operations are supported.
     */
    public static final class ConversationOperations {
        /**
         * ContentValues key used to specify the operation to be performed
         */
        public static final String OPERATION_KEY = "operation";

        /**
         * Archive operation
         */
        public static final String ARCHIVE = "archive";

        /**
         * Mute operation
         */
        public static final String MUTE = "mute";

        /**
         * Report spam operation
         */
        public static final String REPORT_SPAM = "report_spam";

        private ConversationOperations() {
        }
    }

    public static final class DraftType {
        public static final int NOT_A_DRAFT = 0;
        public static final int COMPOSE = 1;
        public static final int REPLY = 2;
        public static final int REPLY_ALL = 3;
        public static final int FORWARD = 4;

        private DraftType() {}
    }

    public static final String[] MESSAGE_PROJECTION = {
        BaseColumns._ID,
        MessageColumns.SERVER_ID,
        MessageColumns.URI,
        MessageColumns.CONVERSATION_ID,
        MessageColumns.SUBJECT,
        MessageColumns.SNIPPET,
        MessageColumns.FROM,
        MessageColumns.TO,
        MessageColumns.CC,
        MessageColumns.BCC,
        MessageColumns.REPLY_TO,
        MessageColumns.DATE_RECEIVED_MS,
        MessageColumns.BODY_HTML,
        MessageColumns.BODY_TEXT,
        MessageColumns.EMBEDS_EXTERNAL_RESOURCES,
        MessageColumns.REF_MESSAGE_ID,
        MessageColumns.DRAFT_TYPE,
        MessageColumns.APPEND_REF_MESSAGE_CONTENT,
        MessageColumns.HAS_ATTACHMENTS,
        MessageColumns.ATTACHMENT_LIST_URI,
        MessageColumns.MESSAGE_FLAGS,
        MessageColumns.JOINED_ATTACHMENT_INFOS,
        MessageColumns.SAVE_MESSAGE_URI,
        MessageColumns.SEND_MESSAGE_URI
    };

    /** Separates attachment info parts in strings in a message. */
    public static final String MESSAGE_ATTACHMENT_INFO_SEPARATOR = "\n";
    public static final String MESSAGE_LIST_TYPE =
            "vnd.android.cursor.dir/vnd.com.android.mail.message";
    public static final String MESSAGE_TYPE =
            "vnd.android.cursor.item/vnd.com.android.mail.message";

    public static final int MESSAGE_ID_COLUMN = 0;
    public static final int MESSAGE_SERVER_ID_COLUMN = 1;
    public static final int MESSAGE_URI_COLUMN = 2;
    public static final int MESSAGE_CONVERSATION_ID_COLUMN = 3;
    public static final int MESSAGE_SUBJECT_COLUMN = 4;
    public static final int MESSAGE_SNIPPET_COLUMN = 5;
    public static final int MESSAGE_FROM_COLUMN = 6;
    public static final int MESSAGE_TO_COLUMN = 7;
    public static final int MESSAGE_CC_COLUMN = 8;
    public static final int MESSAGE_BCC_COLUMN = 9;
    public static final int MESSAGE_REPLY_TO_COLUMN = 10;
    public static final int MESSAGE_DATE_RECEIVED_MS_COLUMN = 11;
    public static final int MESSAGE_BODY_HTML_COLUMN = 12;
    public static final int MESSAGE_BODY_TEXT_COLUMN = 13;
    public static final int MESSAGE_EMBEDS_EXTERNAL_RESOURCES_COLUMN = 14;
    public static final int MESSAGE_REF_MESSAGE_ID_COLUMN = 15;
    public static final int MESSAGE_DRAFT_TYPE_COLUMN = 16;
    public static final int MESSAGE_APPEND_REF_MESSAGE_CONTENT_COLUMN = 17;
    public static final int MESSAGE_HAS_ATTACHMENTS_COLUMN = 18;
    public static final int MESSAGE_ATTACHMENT_LIST_URI_COLUMN = 19;
    public static final int MESSAGE_FLAGS_COLUMN = 20;
    public static final int MESSAGE_JOINED_ATTACHMENT_INFOS_COLUMN = 21;
    public static final int MESSAGE_SAVE_URI_COLUMN = 22;
    public static final int MESSAGE_SEND_URI_COLUMN = 23;

    public static final class MessageFlags {
        public static final int STARRED =       1 << 0;
        public static final int UNREAD =        1 << 1;
        public static final int REPLIED =       1 << 2;
        public static final int FORWARDED =     1 << 3;
    }

    public static final class MessageColumns {
        /**
         * This string column contains a content provider URI that points to this single message.
         */
        public static final String URI = "messageUri";
        /**
         * This string column contains a server-assigned ID for this message.
         */
        public static final String SERVER_ID = "serverMessageId";
        public static final String CONVERSATION_ID = "conversationId";
        /**
         * This string column contains the subject of a message.
         */
        public static final String SUBJECT = "subject";
        /**
         * This string column contains a snippet of the message body.
         */
        public static final String SNIPPET = "snippet";
        /**
         * This string column contains the single email address (and optionally name) of the sender.
         */
        public static final String FROM = "fromAddress";
        /**
         * This string column contains a comma-delimited list of "To:" recipient email addresses.
         */
        public static final String TO = "toAddresses";
        /**
         * This string column contains a comma-delimited list of "CC:" recipient email addresses.
         */
        public static final String CC = "ccAddresses";
        /**
         * This string column contains a comma-delimited list of "BCC:" recipient email addresses.
         * This value will be null for incoming messages.
         */
        public static final String BCC = "bccAddresses";
        /**
         * This string column contains the single email address (and optionally name) of the
         * sender's reply-to address.
         */
        public static final String REPLY_TO = "replyToAddress";
        /**
         * This long column contains the timestamp (in millis) of receipt of the message.
         */
        public static final String DATE_RECEIVED_MS = "dateReceivedMs";
        /**
         * This string column contains the HTML form of the message body, if available. If not,
         * a provider must populate BODY_TEXT.
         */
        public static final String BODY_HTML = "bodyHtml";
        /**
         * This string column contains the plaintext form of the message body, if HTML is not
         * otherwise available. If HTML is available, this value should be left empty (null).
         */
        public static final String BODY_TEXT = "bodyText";
        public static final String EMBEDS_EXTERNAL_RESOURCES = "bodyEmbedsExternalResources";
        /**
         * This string column contains an opaque string used by the sendMessage api.
         */
        public static final String REF_MESSAGE_ID = "refMessageId";
        /**
         * This integer column contains the type of this draft, or zero (0) if this message is not a
         * draft. See {@link DraftType} for possible values.
         */
        public static final String DRAFT_TYPE = "draftType";
        /**
         * This boolean column indicates whether an outgoing message should trigger special quoted
         * text processing upon send. The value should default to zero (0) for protocols that do
         * not support or require this flag, and for all incoming messages.
         */
        public static final String APPEND_REF_MESSAGE_CONTENT = "appendRefMessageContent";
        /**
         * This boolean column indicates whether a message has attachments. The list of attachments
         * can be retrieved using the URI in {@link MessageColumns#ATTACHMENT_LIST_URI}.
         */
        public static final String HAS_ATTACHMENTS = "hasAttachments";
        /**
         * This string column contains the content provider URI for the list of
         * attachments associated with this message.
         */
        public static final String ATTACHMENT_LIST_URI = "attachmentListUri";
        /**
         * This long column is a bit field of flags defined in {@link MessageFlags}.
         */
        public static final String MESSAGE_FLAGS = "messageFlags";
        /**
         * This string column contains a specially formatted string representing all
         * attachments that we added to a message that is being sent or saved.
         */
        public static final String JOINED_ATTACHMENT_INFOS = "joinedAttachmentInfos";
        /**
         * This string column contains the content provider URI for saving this
         * message.
         */
        public static final String SAVE_MESSAGE_URI = "saveMessageUri";
        /**
         * This string column contains content provider URI for sending this
         * message.
         */
        public static final String SEND_MESSAGE_URI = "sendMessageUri";

        private MessageColumns() {}
    }

    // We define a "folder" as anything that contains a list of conversations.
    public static final String ATTACHMENT_LIST_TYPE =
            "vnd.android.cursor.dir/vnd.com.android.mail.attachment";
    public static final String ATTACHMENT_TYPE =
            "vnd.android.cursor.item/vnd.com.android.mail.attachment";

    public static final String[] ATTACHMENT_PROJECTION = {
        BaseColumns._ID,
        AttachmentColumns.NAME,
        AttachmentColumns.SIZE,
        AttachmentColumns.URI,
        AttachmentColumns.ORIGIN_EXTRAS,
        AttachmentColumns.CONTENT_TYPE,
        AttachmentColumns.SYNCED
    };
    private static final String EMAIL_SEPARATOR_PATTERN = "\n";
    public static final int ATTACHMENT_ID_COLUMN = 0;
    public static final int ATTACHMENT_NAME_COLUMN = 1;
    public static final int ATTACHMENT_SIZE_COLUMN = 2;
    public static final int ATTACHMENT_URI_COLUMN = 3;
    public static final int ATTACHMENT_ORIGIN_EXTRAS_COLUMN = 4;
    public static final int ATTACHMENT_CONTENT_TYPE_COLUMN = 5;
    public static final int ATTACHMENT_SYNCED_COLUMN = 6;

    public static final class AttachmentColumns {
        public static final String NAME = "name";
        public static final String SIZE = "size";
        public static final String URI = "uri";
        public static final String ORIGIN_EXTRAS = "originExtras";
        public static final String CONTENT_TYPE = "contentType";
        public static final String SYNCED = "synced";
    }

    public static int getMailMaxAttachmentSize(String account) {
        // TODO: query the account to see what the max attachment size is?
        return 5 * 1024 * 1024;
    }

    public static String getAttachmentTypeSetting() {
        // TODO: query the account to see what kinds of attachments it supports?
        return "com.google.android.gm.allowAddAnyAttachment";
    }

    public static void incrementRecipientsTimesContacted(Context context, String addressString) {
        DataUsageStatUpdater statsUpdater = new DataUsageStatUpdater(context);
        ArrayList<String> recipients = new ArrayList<String>();
        String[] addresses = TextUtils.split(addressString, EMAIL_SEPARATOR_PATTERN);
        for (String address : addresses) {
            recipients.add(address);
        }
        statsUpdater.updateWithAddress(recipients);
    }

    public static final String[] UNDO_PROJECTION = {
        ConversationColumns.MESSAGE_LIST_URI
    };
    public static final int UNDO_MESSAGE_LIST_COLUMN = 0;

    // Parameter used to indicate the sequence number for an undoable operation
    public static final String SEQUENCE_QUERY_PARAMETER = "seq";


    public static final String[] SETTINGS_PROJECTION = {
            SettingsColumns.SIGNATURE,
            SettingsColumns.AUTO_ADVANCE,
            SettingsColumns.MESSAGE_TEXT_SIZE,
            SettingsColumns.SNAP_HEADERS,
            SettingsColumns.REPLY_BEHAVIOR,
            SettingsColumns.HIDE_CHECKBOXES,
            SettingsColumns.CONFIRM_DELETE,
            SettingsColumns.CONFIRM_ARCHIVE,
            SettingsColumns.CONFIRM_SEND,
    };

    public static final class AutoAdvance {
        public static final int UNSET = 0;
        public static final int OLDER = 1;
        public static final int NEWER = 2;
        public static final int LIST = 3;
    }

    public static final class SnapHeaderValue {
        public static final int ALWAYS = 0;
        public static final int PORTRAIT_ONLY = 1;
        public static final int NEVER = 2;
    }

    public static final class MessageTextSize {
        public static final int TINY = -2;
        public static final int SMALL = -1;
        public static final int NORMAL = 0;
        public static final int LARGE = 1;
        public static final int HUGE = 2;
    }

    public static final class DefaultReplyBehavior {
        public static final int REPLY = 0;
        public static final int REPLY_ALL = 1;
    }

    public static final class SettingsColumns {
        /**
         * String column containing the contents of the signature for this account.  If no
         * signature has been specified, the value will be null.
         */
        public static final String SIGNATURE = "signature";

        /**
         * Integer column containing the user's specified auto-advance policy.  This value will be
         * one of the values in {@link UIProvider.AutoAdvance}
         */
        public static final String AUTO_ADVANCE = "auto_advance";

        /**
         * Integer column containing the user's specified message text size preference.  This value
         * will be one of the values in {@link UIProvider.MessageTextSize}
         */
        public static final String MESSAGE_TEXT_SIZE = "message_text_size";

        /**
         * Integer column contaning the user's specified snap header preference.  This value
         * will be one of the values in {@link UIProvider.SnapHeaderValue}
         */
        public static final String SNAP_HEADERS = "snap_headers";

        /**
         * Integer column containing the user's specified default reply behavior.  This value will
         * be one of the values in {@link UIProvider.DefaultReplyBehavior}
         */
        public static final String REPLY_BEHAVIOR = "reply_behavior";

        /**
         * Integer column containing the user's specified checkbox preference.  The  value
         * of 0 indicates that checkboxes are not hidden.
         */
        public static final String HIDE_CHECKBOXES = "hide_checkboxes";

        /**
         * Integer column containing the user's specified confirm delete preference value.
         * A value of 1 indicates that the user has indicated that a confirmation should
         * be shown when a delete action is performed.
         */
        public static final String CONFIRM_DELETE = "confirm_delete";

        /**
         * Integer column containing the user's specified confirm archive preference value.
         * A value of 1 indicates that the user has indicated that a confirmation should
         * be shown when an archive action is performed.
         */
        public static final String CONFIRM_ARCHIVE = "confirm_archive";

        /**
         * Integer column containing the user's specified confirm send preference value.
         * A value of 1 indicates that the user has indicated that a confirmation should
         * be shown when a send action is performed.
         */
        public static final String CONFIRM_SEND = "confirm_send";
    }
}
