package net.akive.api.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "net.akive.api",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

    private static final String ROOT_PACKAGE = "net.akive.api";
    private static final String ADAPTER_PACKAGE = ROOT_PACKAGE + "..adapter..";
    private static final String APPLICATION_PACKAGE = ROOT_PACKAGE + "..application..";
    private static final String DOMAIN_PACKAGE = ROOT_PACKAGE + "..domain..";
    private static final String COMMON_PACKAGE = ROOT_PACKAGE + ".common..";
    private static final String CORE_PACKAGE = ROOT_PACKAGE + ".core..";
    private static final String ENTITY_PACKAGE = ROOT_PACKAGE + "..adapter..persistence..entity..";
    private static final String GENERATED_PACKAGE = ROOT_PACKAGE + "..Q*";

    @ArchTest
    static final ArchRule 레이어는_어댑터_애플리케이션_도메인_순서로_참조해야_한다 =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Adapter").definedBy(ADAPTER_PACKAGE)
                    .layer("Application").definedBy(APPLICATION_PACKAGE)
                    .layer("Domain").definedBy(DOMAIN_PACKAGE)
                    .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Adapter", "Application")
                    .because("hexagonal architecture dependencies should point adapter -> application -> domain");

    @ArchTest
    static final ArchRule 도메인은_애플리케이션과_어댑터를_참조하지_않아야_한다 =
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage(APPLICATION_PACKAGE, ADAPTER_PACKAGE)
                    .because("domain model must not know about use cases or infrastructure");

    @ArchTest
    static final ArchRule 애플리케이션은_어댑터를_참조하지_않아야_한다 =
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER_PACKAGE)
                    .because("application services should use required ports instead of concrete adapters");

    @ArchTest
    static final ArchRule 모듈_간_순환_참조가_없어야_한다 =
            slices()
                    .assignedFrom(new FeatureModuleSliceAssignment())
                    .should().beFreeOfCycles()
                    .because("feature modules such as auth, plan, and mentoring should not form cycles");

    @ArchTest
    static final ArchRule 엔티티를_제외한_클래스_간_순환_참조가_없어야_한다 =
            slices()
                    .assignedFrom(new NonEntityClassSliceAssignment())
                    .should().beFreeOfCycles()
                    .because("class-level cycles make modules harder to change; "
                            + "JPA entity graphs are exempted");

    @ArchTest
    static final ArchRule 도메인은_프레임워크에_의존하지_않아야_한다 =
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .and().resideOutsideOfPackage(COMMON_PACKAGE)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "javax.persistence..",
                            "org.hibernate.."
                    )
                    .because("domain objects should stay persistence and web framework independent");

    @ArchTest
    static final ArchRule provided_port와_required_port는_인터페이스여야_한다 =
            classes()
                    .that().resideInAnyPackage(
                            ROOT_PACKAGE + "..application.provided..",
                            ROOT_PACKAGE + "..application.required.."
                    )
                    .and().resideOutsideOfPackage(ROOT_PACKAGE + "..application..dto..")
                    .and().resideOutsideOfPackage(ROOT_PACKAGE + "..application..data..")
                    .should().beInterfaces()
                    .because("application ports should be expressed as interfaces; "
                            + "dto/data classes are message models");

    @ArchTest
    static final ArchRule 웹_어댑터는_required_port를_참조하지_않아야_한다 =
            noClasses()
                    .that().resideInAPackage(ROOT_PACKAGE + "..adapter..webapi..")
                    .should().dependOnClassesThat().resideInAPackage(ROOT_PACKAGE + "..application..required..")
                    .because("driving adapters should call provided ports, not driven ports");

    @ArchTest
    static final ArchRule 영속성_어댑터는_어댑터_밖에서_참조되지_않아야_한다 =
            noClasses()
                    .that().resideOutsideOfPackage(ADAPTER_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ROOT_PACKAGE + "..adapter..persistence..")
                    .because("persistence details should stay behind required ports");

    private static final class NonEntityClassSliceAssignment implements SliceAssignment {

        private static final DescribedPredicate<JavaClass> CLASSES_EXCLUDED_FROM_CLASS_CYCLE_CHECK =
                JavaClass.Predicates.resideInAnyPackage(COMMON_PACKAGE, CORE_PACKAGE, ENTITY_PACKAGE)
                        .or(JavaClass.Predicates.resideInAPackage(GENERATED_PACKAGE));

        @Override
        public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
            if (CLASSES_EXCLUDED_FROM_CLASS_CYCLE_CHECK.test(javaClass)
                    || javaClass.isNestedClass()
                    || javaClass.getSimpleName().startsWith("Q")) {
                return SliceIdentifier.ignore();
            }

            return SliceIdentifier.of(javaClass.getName());
        }

        @Override
        public String getDescription() {
            return "non-entity classes";
        }
    }

    private static final class FeatureModuleSliceAssignment implements SliceAssignment {

        private static final DescribedPredicate<JavaClass> CLASSES_EXCLUDED_FROM_MODULE_CYCLE_CHECK =
                JavaClass.Predicates.resideInAnyPackage(COMMON_PACKAGE, CORE_PACKAGE, ENTITY_PACKAGE)
                        .or(JavaClass.Predicates.resideInAPackage(GENERATED_PACKAGE));

        @Override
        public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
            if (CLASSES_EXCLUDED_FROM_MODULE_CYCLE_CHECK.test(javaClass)
                    || javaClass.getSimpleName().startsWith("Q")
                    || !javaClass.getPackageName().startsWith(ROOT_PACKAGE + ".")) {
                return SliceIdentifier.ignore();
            }

            String packageName = javaClass.getPackageName();
            String packageBelowRoot = packageName.substring(ROOT_PACKAGE.length() + 1);
            if (!packageBelowRoot.contains(".")) {
                return SliceIdentifier.ignore();
            }

            String featureModule = packageBelowRoot.split("\\.")[0];

            return SliceIdentifier.of(featureModule);
        }

        @Override
        public String getDescription() {
            return "feature modules";
        }
    }
}
