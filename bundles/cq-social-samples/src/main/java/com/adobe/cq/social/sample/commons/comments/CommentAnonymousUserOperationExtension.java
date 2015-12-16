/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2013 Adobe Systems Incorporated
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.commons.comments.api.Comment;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperationExtension;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.ClientUtilityFactory;
import com.adobe.cq.social.scf.Operation;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.ugcbase.CollabUser;
import com.adobe.cq.social.ugcbase.SocialUtils;
import com.adobe.granite.xss.XSSAPI;

@Component(immediate = true)
@Service
public class CommentAnonymousUserOperationExtension implements CommentOperationExtension {

    private final String OPERATION_EXTENSION_NAME = "Comment Anonymous User Operation Extention";
    private final int OPERATION_EXTENSION_ORDER = 10;
    public static final String USER_ID = "custom-user-id";
    @Reference
    ClientUtilityFactory clientUtilFactory;

    @Reference
    private SocialUtils socialUtils;

    /** Reference to <code>XSSAPI</code>. */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private XSSAPI xss;
    
    private static final Logger LOG = LoggerFactory.getLogger(CommentAnonymousUserOperationExtension.class);

    @Override
    public int getOrder() {
        return OPERATION_EXTENSION_ORDER;
    }

    @Override
    public String getName() {
        return OPERATION_EXTENSION_NAME;
    }

    @Override
    public void beforeAction(final Operation operation, final Session session, final Resource resource,
        final Map<String, Object> requestParameters) throws OperationException {
        // no-op
    }

    @Override
    public void afterAction(final Operation operation, final Session session, final Comment comment,
        final Map<String, Object> requestParameters) throws OperationException {
        String userId;

        if (requestParameters.containsKey(USER_ID)) {
            userId = requestParameters.get(USER_ID).toString();
            
        } 
        else {
            userId = "anonymous";
        }
        if (StringUtils.isNotBlank(userId)) {
            final ResourceResolver resolver = comment.getResource().getResourceResolver();
            ClientUtilities clientUtils = getClientUtilities(comment.getResource().getResourceResolver());
            try {
                Resource postResource = comment.getResource();
                final ModifiableValueMap vm = postResource.adaptTo(ModifiableValueMap.class);
                vm.put(CollabUser.PROP_NAME, userId);
                vm.put("authorizableId", userId);
                resolver.commit();
            } catch (PersistenceException e) {
                LOG.error("PersistenceException when trying to change author info by modifying the userIdenfier property"
                        + e.getMessage());
            }
        }
    }

    @Override
    public List<CommentOperation> getOperationsToHookInto() {
        return Arrays.asList(CommentOperation.CREATE);
    }

    protected ClientUtilities getClientUtilities(final ResourceResolver resourceResolver) {
        return clientUtilFactory.getClientUtilities(this.xss, resourceResolver, socialUtils);
    }
}
