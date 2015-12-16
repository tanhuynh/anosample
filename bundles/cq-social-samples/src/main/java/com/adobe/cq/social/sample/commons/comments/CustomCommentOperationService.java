/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2015 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.cq.social.sample.commons.comments;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.CommentSystem;
import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.endpoints.AbstractCommentOperationService;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperationExtension;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperationExtension.CommentOperation;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperations;
import com.adobe.cq.social.commons.events.CommentEvent;
import com.adobe.cq.social.commons.events.CommentEvent.CommentActions;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.SocialComponentFactoryManager;
import com.adobe.cq.social.srp.SocialResource;
import com.adobe.cq.social.ugcbase.SocialUtils;

/**
 * Sling Operation for comment creation.
 */
@Component(metatype = true, label = "AEM Communities CommentOperationProvider",
        description = "This component serves the POST comments")
@Service
@Reference(name = "commentOperationExtension", referenceInterface = CommentOperationExtension.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
public class CustomCommentOperationService extends
    AbstractCommentOperationService<CommentOperationExtension, CommentOperationExtension.CommentOperation, Comment>
    implements CommentOperations {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CustomCommentOperationService.class);

    @Reference
    private SocialComponentFactoryManager factoryManager;

    @Reference
    private SocialUtils socialUtils;

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getResourceType(final Resource targetResource) {
        final Resource componentResource;
        if (targetResource instanceof SocialResource) {
            componentResource = ((SocialResource) targetResource).getRootJCRNode();
        } else {
            componentResource = targetResource;
        }
        return socialUtils.getResourceTypeFromDesign(componentResource, SocialUtils.PN_COMMENT_RESOURCETYPE,
            com.adobe.cq.social.commons.comments.api.Comment.COMMENT_RESOURCETYPE);
    }

    @Override
    protected void postCreateEvent(final com.adobe.cq.social.commons.comments.api.Comment comment, final String userId) {
        postEvent(new CommentEvent(comment, userId, comment.isTopLevel() ? CommentActions.CREATED
            : CommentActions.REPLIED));
    }

    @Override
    protected void postDeleteEvent(final com.adobe.cq.social.commons.comments.api.Comment comment, final String userId) {
        postEvent(new CommentEvent(comment, userId, CommentActions.DELETED));
    }

    @Override
    protected void postUpdateEvent(final com.adobe.cq.social.commons.comments.api.Comment comment, final String userId) {
        postEvent(new CommentEvent(comment, userId, CommentActions.EDITED));
    }

    @Override
    protected CommentOperation getCreateOperation() {
        return CommentOperation.CREATE;
    }

    /**
     * OSGi bind {@link CommentOperationExtension} handler.
     * @param extension the commentOperationExtension
     */
    protected void bindCommentOperationExtension(final CommentOperationExtension extension) {
        addOperationExtension(extension);
    }

    /**
     * OSGi unbind {@link CommentOperationExtension} handler.
     * @param extension the commentOperationExtension
     */
    protected void unbindCommentOperationExtension(final CommentOperationExtension extension) {
        removeOperationExtension(extension);
    }

    @Override
    protected CommentOperation getUpdateOperation() {
        return CommentOperation.UPDATE;
    }

    @Override
    protected CommentOperation getDeleteOperation() {
        return CommentOperation.DELETE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Comment getSocialComponentForResource(final Resource resource) {
        if (resource == null) {
            return null;
        }
        final SocialComponentFactory factory = this.factoryManager.getSocialComponentFactory(resource);
        final SocialComponent component = factory.getSocialComponent(resource);
        if (component instanceof Comment) {
            return (Comment) component;
        } else {
            return null;
        }
    }

    @Override
    protected CommentOperation getUploadImageOperation() {
        return CommentOperation.UPLOADIMAGE;
    }
    
    /**
     * Override this method to allow anonymous user to post comment
     */
    @Override
    protected boolean mayPost(final SlingHttpServletRequest request, final CommentSystem cs, final String userId) {
        return true;
    }
    
}
