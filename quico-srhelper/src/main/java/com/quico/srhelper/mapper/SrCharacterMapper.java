package com.quico.srhelper.mapper;

import java.util.List;
import java.util.Date;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.vo.CelestialCharacterVO;
import com.quico.srhelper.domain.vo.LimitedCharacterOptionVO;
import org.apache.ibatis.annotations.Param;

/**
 * 角色Mapper接口
 *
 * @author quico
 * @date 2026-05-12
 */
public interface SrCharacterMapper
{
    /**
     * 查询角色
     *
     * @param id 角色主键
     * @return 角色
     */
    public SrCharacter selectSrCharacterById(Long id);

    /**
     * 按角色名字 + 实装版本精确查询（导入去重用）
     *
     * @param characterName 角色名字
     * @param releaseVersion 实装版本（可为空）
     * @return 角色，无匹配返回 null
     */
    public SrCharacter selectSrCharacterByNameAndVersion(@Param("characterName") String characterName,
                                                         @Param("releaseVersion") String releaseVersion);

    /**
     * 按角色名字精确查询全部记录（材料绑定导入用：同名多版本时返回多条，由业务层判定）
     *
     * @param characterName 角色名字
     * @return 角色列表
     */
    public List<SrCharacter> selectSrCharacterListByName(@Param("characterName") String characterName);

    /**
     * 查询角色列表
     * 
     * @param srCharacter 角色
     * @return 角色集合
     */
    public List<SrCharacter> selectSrCharacterList(SrCharacter srCharacter);

    /**
     * 新增角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    public int insertSrCharacter(SrCharacter srCharacter);

    /**
     * 修改角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    public int updateSrCharacter(SrCharacter srCharacter);

    /**
     * 查询星缘相遇角色列表（SQL 强制 standard_type = 2，禁止全量返回后由前端过滤）
     *
     * @param characterName 角色名字（可选，模糊匹配）
     * @return 星缘相遇角色精简视图
     */
    public List<CelestialCharacterVO> selectCelestialCharacterList(@Param("characterName") String characterName);

    /**
     * 查询可选限定角色下拉选项（SQL 强制 standard_type = 0）
     *
     * @param characterName 角色名字（可选，模糊匹配）
     * @return 限定角色选项
     */
    public List<LimitedCharacterOptionVO> selectLimitedCharacterOptions(@Param("characterName") String characterName);

    /**
     * 批量更新角色常驻类型与加入常驻池时间（星缘相遇用）
     *
     * @param ids 角色主键集合
     * @param standardType 常驻类型（StandardType.code）
     * @param standardStartTime 加入常驻池时间
     * @return 结果
     */
    public int batchUpdateStandardType(@Param("ids") Long[] ids,
                                       @Param("standardType") Integer standardType,
                                       @Param("standardStartTime") Date standardStartTime);

    /**
     * 删除角色
     * 
     * @param id 角色主键
     * @return 结果
     */
    public int deleteSrCharacterById(Long id);

    /**
     * 批量删除角色
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrCharacterByIds(Long[] ids);
}
