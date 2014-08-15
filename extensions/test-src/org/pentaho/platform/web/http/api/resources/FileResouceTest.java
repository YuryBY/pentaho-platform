package org.pentaho.platform.web.http.api.resources;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.importexport.Exporter;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class FileResouceTest {

  FileResource fileResource;

  @Before
  public void setUp() {
    fileResource = spy( new FileResource() );
    fileResource.fileService = mock( FileService.class );
    fileResource.httpServletRequest = mock( HttpServletRequest.class );
    fileResource.policy = mock( IAuthorizationPolicy.class );
    fileResource.repository = mock( IUnifiedRepository.class );
    fileResource.repoWs = mock( DefaultUnifiedRepositoryWebService.class );
  }

  @After
  public void tearDown() {
    fileResource = null;
  }

  @Test
  public void doDeleteFiles() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doDeleteFiles( eq( params ) );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doDeleteFiles( params );

    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doDeleteFiles( params );
    assertEquals( testResponse, mockResponse );
  }

  @Test
  public void testDoDeleteFilesError() throws Exception {
    Throwable mockException = mock( RuntimeException.class );
    String params = "params";

    doThrow( mockException ).when( fileResource.fileService ).doDeleteFiles( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.doDeleteFiles( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doDeleteFiles( params );
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doDeleteFilesPermanent( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doDeleteFilesPermanent( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doDeleteFilesPermanent( params );
  }

  @Test
  public void testDoDeleteFilesPermanentError() throws Exception {
    Throwable mockException = mock( RuntimeException.class );
    String params = "params";

    doThrow( mockException ).when( fileResource.fileService ).doDeleteFilesPermanent( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.doDeleteFilesPermanent( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doDeleteFilesPermanent( params );
    verify( mockException, times( 1 ) ).printStackTrace();
  }

  @Test
  public void testDoMove() throws Exception {
    String pathId = "pathId";
    String params = "params";
    doNothing().when( fileResource.fileService ).doMoveFiles( pathId, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doMove( pathId, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doMoveFiles( pathId, params );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testDoMoveError() throws Exception {
    // Test 1
    String pathId = "pathId";
    String params = "params";

    doThrow( mock( FileNotFoundException.class ) ).when( fileResource.fileService ).doMoveFiles( pathId, params );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource )
      .buildStatusResponse( INTERNAL_SERVER_ERROR );

    Response testResponse = fileResource.doMove( pathId, params );

    assertEquals( mockNotFoundResponse, testResponse );

    // Test 2
    doThrow( mock( RuntimeException.class ) ).when( fileResource.fileService ).doMoveFiles( pathId, params );

    testResponse = fileResource.doMove( pathId, params );

    assertEquals( mockInternalServerErrorResponse, testResponse );
  }

  @Test
  public void testDoRestore() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doRestoreFiles( eq( params ) );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doRestore( params );

    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doRestoreFiles( params );
    assertEquals( testResponse, mockResponse );
  }

  @Test
  public void testDoRestoreError() {
    String params = "params";

    doThrow( mock( InternalError.class ) ).when( fileResource.fileService ).doRestoreFiles( params );

    Response mockInternalErrorResponse = mock( Response.class );
    doReturn( mockInternalErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Response testResponse = fileResource.doRestore( params );

    assertEquals( mockInternalErrorResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doRestoreFiles( params );
    verify( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );
  }

  @Test
  public void testCreateFile() throws Exception {
    String pathId = "pathId";
    String charsetName = "charsetName";
    InputStream mockInputStream = mock( InputStream.class );

    doNothing().when( fileResource.fileService ).createFile( charsetName, pathId, mockInputStream );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    doReturn( charsetName ).when( fileResource.httpServletRequest ).getCharacterEncoding();

    Response testResponse = fileResource.createFile( pathId, mockInputStream );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).createFile( charsetName, pathId, mockInputStream );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testCreateFileError() throws Exception {
    String charsetName = "charsetName";
    String pathId = "pathId";
    InputStream mockInputStream = mock( InputStream.class );

    Exception mockException = mock( RuntimeException.class );

    doThrow( mockException ).when( fileResource.fileService )
      .createFile( charsetName, pathId, mockInputStream );

    doReturn( charsetName ).when( fileResource.httpServletRequest ).getCharacterEncoding();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.createFile( pathId, mockInputStream );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildServerErrorResponse( mockException );
    verify( fileResource.httpServletRequest, times( 1 ) ).getCharacterEncoding();
    verify( fileResource.fileService ).createFile( charsetName, pathId, mockInputStream );
  }

  @Test
  public void testDoCopyFiles() {
    String pathId = "pathId";
    Integer mode = 1;
    String params = "params";

    doNothing().when( fileResource.fileService ).doCopyFiles( pathId, mode, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doCopyFiles( pathId, mode, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doCopyFiles( pathId, mode, params );
  }

  @Test
  public void testDoCopyFilesError() {
    String pathId = "pathId";
    Integer mode = 1;
    String params = "params";

    Exception mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( fileResource.fileService ).doCopyFiles( pathId, mode, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildSafeHtmlServerErrorResponse( mockException );

    Response testResponse = fileResource.doCopyFiles( pathId, mode, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildSafeHtmlServerErrorResponse( mockException );
    verify( fileResource.fileService ).doCopyFiles( pathId, mode, params );
  }

  @Test
  public void testDoGetFileOrDir() throws Exception {
    String pathId = "pathId";

    FileService.RepositoryFileToStreamWrapper mockWrapper = mock( FileService.RepositoryFileToStreamWrapper.class );
    doReturn( mockWrapper ).when( fileResource.fileService ).doGetFileOrDir( pathId );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockWrapper );

    Response testResponse = fileResource.doGetFileOrDir( pathId );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDir( pathId );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockWrapper );
  }

  @Test
  public void testDoGetFileOrDirError() throws Exception {
    // Test 1
    String pathId = "pathId";

    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileOrDir( pathId );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    Response testResponse = fileResource.doGetFileOrDir( pathId );

    assertEquals( mockNotFoundResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( NOT_FOUND );
    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDir( pathId );

    // Test 2
    Exception mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( fileResource.fileService ).doGetFileOrDir( pathId );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    testResponse = fileResource.doGetFileOrDir( pathId );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( NOT_FOUND );
    verify( fileResource.fileService, times( 2 ) ).doGetFileOrDir( pathId );
  }

  @Test
  public void testDoGetDirAsZipWithPathId() {
    String pathId = "pathId";

    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( pathId );

    doReturn( true ).when( fileResource ).isPathValid( path );

    doReturn( true ).when( fileResource.policy ).isAllowed( PublishAction.NAME );

    RepositoryFile mockFile = mock( RepositoryFile.class );
    doReturn( mockFile ).when( fileResource.repository ).getFile( path );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).doGetDirAsZip( mockFile );

    Response testResponse = fileResource.doGetDirAsZip( pathId );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.repository, times( 1 ) ).getFile( path );
    verify( fileResource.policy, times( 1 ) ).isAllowed( PublishAction.NAME );
    verify( fileResource, times( 1 ) ).isPathValid( path );
    verify( fileResource.fileService, times( 1 ) ).idToPath( pathId );
  }

  @Test
  public void testDoGetDirAsZipWithPathIdError() {
    // Test 1
    String pathId = "pathId";
    String path = "path";

    doReturn( path ).when( fileResource.fileService ).idToPath( pathId );

    doReturn( false ).when( fileResource ).isPathValid( path );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response testResponse = fileResource.doGetDirAsZip( pathId );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( FORBIDDEN );
    verify( fileResource, times( 1 ) ).isPathValid( path );

    // Test 2
    doReturn( true ).when( fileResource ).isPathValid( path );
    doReturn( false ).when( fileResource.policy ).isAllowed( PublishAction.NAME );

    testResponse = fileResource.doGetDirAsZip( pathId );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 2 ) ).buildStatusResponse( FORBIDDEN );
    verify( fileResource, times( 2 ) ).isPathValid( path );
    verify( fileResource.policy, times( 1 ) ).isAllowed( PublishAction.NAME );

    // Test 3
    doReturn( true ).when( fileResource.policy ).isAllowed( PublishAction.NAME );
    doReturn( null ).when( fileResource.repository ).getFile( path );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    testResponse = fileResource.doGetDirAsZip( pathId );

    assertEquals( mockNotFoundResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( NOT_FOUND );
    verify( fileResource, times( 3 ) ).isPathValid( path );
    verify( fileResource.policy, times( 2 ) ).isAllowed( PublishAction.NAME );
  }

  @Test
  public void testDoGetDirAsZipWithFile() throws Exception {
    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );

    String path = "path";
    doReturn( path ).when( mockRepositoryFile ).getPath();

    Exporter mockExporter = mock( Exporter.class );
    doReturn( mockExporter ).when( fileResource ).getExporter();

    File mockFile = mock( File.class );
    doReturn( mockFile ).when( mockExporter ).doExportAsZip( mockRepositoryFile );

    InputStream mockInputStream = mock( FileInputStream.class );
    doReturn( mockInputStream ).when( fileResource ).getFileInputStream( mockFile );

    StreamingOutput mockOutput = mock( StreamingOutput.class );
    doReturn( mockOutput ).when( fileResource ).getStreamingOutput( mockInputStream );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockOutput, FileResource.APPLICATION_ZIP );

    Response testResponse = fileResource.doGetDirAsZip( mockRepositoryFile );

    assertEquals( mockResponse, testResponse );
    verify( mockRepositoryFile, times( 1 ) ).getPath();
    verify( fileResource, times( 1 ) ).getExporter();
    verify( mockExporter, times( 1 ) ).setRepoPath( path );
    verify( mockExporter, times( 1 ) ).setRepoWs( fileResource.repoWs );
    verify( mockExporter, times( 1 ) ).doExportAsZip( mockRepositoryFile );
    verify( fileResource, times( 1 ) ).getFileInputStream( mockFile );
    verify( fileResource, times( 1 ) ).getStreamingOutput( mockInputStream );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockOutput, FileResource.APPLICATION_ZIP );
  }

  @Test
  public void testDoGetDirAsZipWithFileError() {
    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );

    String path = "path";
    doReturn( path ).when( mockRepositoryFile ).getPath();

    Exception mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( fileResource ).getExporter();

    String exceptionToString = "exception";
    doReturn( exceptionToString ).when( mockException ).toString();

    Response mockServerErrorResponse = mock( Response.class );
    doReturn( mockServerErrorResponse ).when( fileResource ).buildServerErrorResponse( exceptionToString );
  }

  @Test
  public void testDoIsParameterizable() throws Exception {
    String pathId = "pathId";

    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( pathId );

    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );
    doReturn( mockRepositoryFile ).when( fileResource.repository ).getFile( path );

    doReturn( true ).when( fileResource ).hasParameterUi( mockRepositoryFile );

    IContentGenerator mockContentGenerator = mock( IContentGenerator.class );
    doReturn( mockContentGenerator ).when( fileResource ).getContentGenerator( mockRepositoryFile );

    SimpleParameterProvider mockSimpleParameterProvider = mock( SimpleParameterProvider.class );
    doReturn( mockSimpleParameterProvider ).when( fileResource ).getSimpleParameterProvider();

    String repositoryPath = "repositoryPath";
    doReturn( repositoryPath ).when( mockRepositoryFile ).getPath();

    String encodedPath = "encodedPath";
    doReturn( encodedPath ).when( fileResource ).encode( repositoryPath );

    IPentahoSession mockPentahoSession = mock( IPentahoSession.class );
    doReturn( mockPentahoSession ).when( fileResource ).getSession();

    ByteArrayOutputStream mockByteArrayOutputStream = mock( ByteArrayOutputStream.class );
    doReturn( mockByteArrayOutputStream ).when( fileResource ).getByteArrayOutputStream();

    doReturn( 1 ).when( mockByteArrayOutputStream ).size();

    String outputStreamToString = "outputStreamToString";
    doReturn( outputStreamToString ).when( mockByteArrayOutputStream ).toString();

    Document mockDocument = mock( Document.class );
    doReturn( mockDocument ).when( fileResource ).parseText( outputStreamToString );

    String selectNodesParam = "parameters/parameter";
    List<Element> elements = new ArrayList<Element>();
    doReturn( elements ).when( mockDocument ).selectNodes( selectNodesParam );

    Element mockElement = mock( Element.class );
    doReturn( "output-target" ).when( mockElement ).attributeValue( "name" );
    doReturn( "true" ).when( mockElement ).attributeValue( "is-mandatory" );

    // Test 1
    String testString = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.FALSE.toString(), testString );

    // Test 2
    elements.add( mockElement );

    testString = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.TRUE.toString(), testString );

    // Test 3
    doReturn( "false" ).when( mockElement ).attributeValue( "is-mandatory" );

    testString = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.TRUE.toString(), testString );

    // Test 4
    Element mockAttribElement = mock( Element.class );
    doReturn( mockAttribElement ).when( mockElement ).selectSingleNode( "attribute[@namespace='http://reporting.pentaho"
      + ".org/namespaces/engine/parameter-attributes/core' and @name='role']" );

    testString = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.TRUE.toString(), testString );

    verify( fileResource.fileService, times( 4 ) ).idToPath( pathId );
    verify( fileResource.repository, times( 4 ) ).getFile( path );
    verify( fileResource, times( 4 ) ).hasParameterUi( mockRepositoryFile );
    verify( fileResource, times( 4 ) ).getContentGenerator( mockRepositoryFile );
    verify( mockContentGenerator, times( 4 ) ).setOutputHandler( any( SimpleOutputHandler.class ) );
    verify( mockContentGenerator, times( 4 ) ).setMessagesList( anyList() );
    verify( fileResource, times( 4 ) ).getSimpleParameterProvider();
    verify( mockRepositoryFile, times( 4 ) ).getPath();
    verify( fileResource, times( 4 ) ).encode( repositoryPath );
    verify( mockSimpleParameterProvider, times( 4 ) ).setParameter( "path", encodedPath );
    verify( mockSimpleParameterProvider, times( 4 ) ).setParameter( "renderMode", "PARAMETER" );
    verify( mockContentGenerator, times( 4 ) ).setParameterProviders( anyMap() );
    verify( fileResource, times( 4 ) ).getSession();
    verify( mockContentGenerator, times( 4 ) ).setSession( mockPentahoSession );
    verify( mockContentGenerator, times( 4 ) ).createContent();
    verify( fileResource, times( 4 ) ).getByteArrayOutputStream();
    verify( mockDocument, times( 4 ) ).selectNodes( selectNodesParam );
    verify( mockElement, times( 3 ) ).attributeValue( "name" );
    verify( mockElement, times( 3 ) ).attributeValue( "is-mandatory" );
    verify( mockAttribElement, times( 1 ) ).attributeValue( "value" );
  }

  @Test
  public void testDoIsParameterizableError() throws Exception {
    String pathId = "pathId";

    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( pathId );

    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );
    doReturn( mockRepositoryFile ).when( fileResource.repository ).getFile( path );

    Exception mockNoSuchBeanDefinitionException = mock( NoSuchBeanDefinitionException.class );
    doThrow( mockNoSuchBeanDefinitionException ).when( fileResource ).hasParameterUi( mockRepositoryFile );

    String exceptionMessage = "exceptionMessage";
    doReturn( exceptionMessage ).when( mockNoSuchBeanDefinitionException ).getMessage();

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    String message = "message";
    String key = "FileResource.PARAM_FAILURE";
    doReturn( message ).when( mockMessages ).getString( key, exceptionMessage );

    // Test 1
    String testResult = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.FALSE.toString(), testResult );

    // Test 2
    doReturn( true ).when( fileResource ).hasParameterUi( mockRepositoryFile );
    doThrow( mockNoSuchBeanDefinitionException ).when( fileResource ).getContentGenerator( mockRepositoryFile );

    testResult = fileResource.doIsParameterizable( pathId );
    assertEquals( Boolean.FALSE.toString(), testResult );

    verify( fileResource.fileService, times( 2 ) ).idToPath( pathId );
    verify( fileResource.repository, times( 2 ) ).getFile( path );
    verify( fileResource, times( 2 ) ).hasParameterUi( mockRepositoryFile );
    verify( mockNoSuchBeanDefinitionException, times( 1 ) ).getMessage();
    verify( mockMessages, times( 1 ) ).getString( key, exceptionMessage );
  }

  @Test
  public void testDoGetFileOrDirAsDownload() throws Throwable {
    String userAgent = "userAgent";
    String pathId = "pathId";
    String strWithManifest = "strWithManifest";

    FileService.DownloadFileWrapper mockDownloadFileWrapper = mock( FileService.DownloadFileWrapper.class );
    doReturn( mockDownloadFileWrapper ).when( fileResource.fileService )
      .doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildZipOkResponse( mockDownloadFileWrapper );

    Response testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    verify( fileResource ).buildZipOkResponse( mockDownloadFileWrapper );
  }

  @Test
  public void testDoGetFileOrDirAsDownloadError() throws Throwable {
    String userAgent = "userAgent";
    String pathId = "pathId";
    String strWithManifest = "strWithManifest";

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    Response mockBadRequestResponse = mock( Response.class );
    doReturn( mockBadRequestResponse ).when( fileResource ).buildStatusResponse( BAD_REQUEST );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    String exceptionMessage = "exception";

    // Test 1
    Exception mockInvalidParameterException = mock( InvalidParameterException.class );
    doThrow( mockInvalidParameterException ).when( fileResource.fileService )
      .doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    doReturn( exceptionMessage ).when( mockInvalidParameterException ).getMessage();

    Response testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockBadRequestResponse, testResponse );

    // Test 2
    Exception mockIllegalSelectorException = mock( IllegalSelectorException.class );
    doThrow( mockIllegalSelectorException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent,
      pathId, strWithManifest );
    doReturn( exceptionMessage ).when( mockIllegalSelectorException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 3
    Exception mockGeneralSecurityException = mock( GeneralSecurityException.class );
    doThrow( mockGeneralSecurityException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent,
      pathId, strWithManifest );
    doReturn( exceptionMessage ).when( mockGeneralSecurityException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 4
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent, pathId,
      strWithManifest );
    doReturn( exceptionMessage ).when( mockFileNotFoundException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 5
    Throwable mockThrowable = mock( Throwable.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent, pathId,
      strWithManifest );
    doReturn( exceptionMessage ).when( mockThrowable ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
    assertEquals( mockInternalServerErrorResponse, testResponse );

    verify( mockMessages, times( 4 ) ).getString( "FileResource.EXPORT_FAILED", exceptionMessage );
    verify( mockMessages, times( 1 ) ).getString( "FileResource.EXPORT_FAILED", pathId + " " + exceptionMessage );
  }

  @Test
  public void testDoGetFileAsInline() throws Exception {
    String pathId = "pathId";

    FileService.RepositoryFileToStreamWrapper mockWrapper = mock( FileService.RepositoryFileToStreamWrapper.class );
    doReturn( mockWrapper ).when( fileResource.fileService ).doGetFileAsInline( pathId );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockWrapper );

    Response testResponse = fileResource.doGetFileAsInline( pathId );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doGetFileAsInline( pathId );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockWrapper );
  }

  @Test
  public void testDoGetFileAsInlineError() throws Exception {
    String pathId = "pathId";

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    Response mockInternalServereErrorResponse = mock( Response.class );
    doReturn( mockInternalServereErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    Exception mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( fileResource.fileService ).doGetFileAsInline( pathId );

    Response testResponse = fileResource.doGetFileAsInline( pathId );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 2
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileAsInline( pathId );

    testResponse = fileResource.doGetFileAsInline( pathId );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 3
    Error mockInternalError = mock( InternalError.class );
    doThrow( mockInternalError ).when( fileResource.fileService ).doGetFileAsInline( pathId );

    testResponse = fileResource.doGetFileAsInline( pathId );
    assertEquals( mockInternalServereErrorResponse, testResponse );

    verify( mockMessages, times( 3 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }

  @Test
  public void testSetFileAcls() throws Exception {
    String pathId = "pathId";
    RepositoryFileAclDto mockRepositoryFileAclDto = mock( RepositoryFileAclDto.class );

    doNothing().when( fileResource.fileService ).setFileAcls( pathId, mockRepositoryFileAclDto );

    Response mockOkResponse = mock( Response.class );
    doReturn( mockOkResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.setFileAcls( pathId, mockRepositoryFileAclDto );
    assertEquals( mockOkResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).setFileAcls( pathId, mockRepositoryFileAclDto );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testSetFileAclsError() throws Exception {
    String pathId = "pathId";
    RepositoryFileAclDto mockRepositoryFileAclDto = mock( RepositoryFileAclDto.class );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Exception mockRuntimeException = mock( RuntimeException.class );
    doThrow( mockRuntimeException ).when( fileResource.fileService ).setFileAcls( pathId, mockRepositoryFileAclDto );

    Response testResponse = fileResource.setFileAcls( pathId, mockRepositoryFileAclDto );
    assertEquals( mockInternalServerErrorResponse, testResponse );

    verify( fileResource, times( 1 ) ).getMessagesInstance();
    verify( fileResource, times( 1 ) ).buildStatusResponse( INTERNAL_SERVER_ERROR );
    verify( fileResource.fileService, times( 1 ) ).setFileAcls( pathId, mockRepositoryFileAclDto );
  }

  @Test
  public void testSetContentCreator() throws Exception {
    String pathId = "pathId";
    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );

    doNothing().when( fileResource.fileService ).doSetContentCreator( pathId, mockRepositoryFileDto );

    Response mockOkResponse = mock( Response.class );
    doReturn( mockOkResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doSetContentCreator( pathId, mockRepositoryFileDto );
    assertEquals( mockOkResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doSetContentCreator( pathId, mockRepositoryFileDto );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testSetContentCreatorError() throws Exception {
    String pathId = "pathId";
    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService )
      .doSetContentCreator( pathId, mockRepositoryFileDto );

    Response testResponse = fileResource.doSetContentCreator( pathId, mockRepositoryFileDto );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doSetContentCreator( pathId, mockRepositoryFileDto );

    testResponse = fileResource.doSetContentCreator( pathId, mockRepositoryFileDto );
    assertEquals( mockInternalServerErrorResponse, testResponse );

    verify( fileResource, times( 1 ) ).buildStatusResponse( NOT_FOUND );
    verify( fileResource, times( 1 ) ).buildStatusResponse( INTERNAL_SERVER_ERROR );
    verify( fileResource.fileService, times( 2 ) ).doSetContentCreator( pathId, mockRepositoryFileDto );
    verify( mockMessages, times( 1 ) ).getErrorString( "FileResource.FILE_NOT_FOUND", pathId );
    verify( mockMessages, times( 1 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }
}